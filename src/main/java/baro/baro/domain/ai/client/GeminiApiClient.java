package baro.baro.domain.ai.client;

import baro.baro.domain.ai.dto.external.GeminiImageRequest;
import baro.baro.domain.ai.dto.external.GeminiImageResponse;
import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.exception.AiQuotaExceededException;
import baro.baro.domain.ai.processing.ImageProcessingService;
import baro.baro.domain.ai.service.RateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiApiClient {

    private final WebClient webClient;
    private final RateLimiter rateLimiter;
    private final ImageProcessingService imageProcessingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.gemini.api.url:}")
    private String geminiImageUrl;

    @Value("${google.gemini.api.key:}")
    private String geminiApiKey;

    @Value("${google.gemini.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${google.gemini.retry.initial-delay-seconds:5}")
    private int retryDelaySeconds;

    @Value("${google.gemini.quota.enabled:true}")
    private boolean quotaCheckEnabled;

    @Value("${google.gemini.quota.rpm:10}")
    private int quotaRpm;

    @Value("${google.gemini.quota.rpd:100}")
    private int quotaRpd;

    public byte[] generateImage(String base64Image, String mimeType, String prompt) {
        log.info("Gemini Image Edit API 호출 시작 - MIME: {}, Prompt: {}",
                mimeType, prompt.substring(0, Math.min(50, prompt.length())));

        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            log.warn("Gemini API Key가 설정되지 않음 - Placeholder 이미지 반환");
            return imageProcessingService.generatePlaceholderImage();
        }

        if (quotaCheckEnabled && !rateLimiter.tryAcquire(quotaRpm, quotaRpd)) {
            long waitTime = rateLimiter.getWaitTimeSeconds(quotaRpm);
            log.error("Rate Limit 초과 - 현재 RPM: {}/{}, RPD: {}/{}, 대기 시간: {}초",
                    rateLimiter.getCurrentRpm(), quotaRpm,
                    rateLimiter.getCurrentRpd(), quotaRpd,
                    waitTime);
            return imageProcessingService.generatePlaceholderImage();
        }

        return Mono.fromCallable(() -> requestImage(base64Image, mimeType, prompt))
                .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(retryDelaySeconds))
                        .filter(throwable -> {
                            // Quota 초과 또는 안전 필터 차단 시 재시도
                            if (throwable instanceof AiQuotaExceededException) {
                                return true;
                            }
                            if (throwable instanceof AiException) {
                                AiException aiEx = (AiException) throwable;
                                return aiEx.getAiErrorCode() == AiErrorCode.IMAGE_BLOCKED_BY_FILTER;
                            }
                            return false;
                        })
                        .doBeforeRetry(retrySignal -> {
                            Throwable failure = retrySignal.failure();
                            if (failure instanceof AiQuotaExceededException) {
                                AiQuotaExceededException ex = (AiQuotaExceededException) failure;
                                Integer suggestedDelay = ex.getRetryAfterSeconds();
                                log.warn("Gemini API Quota 초과 - Retry {}/{}, {}초 후 재시도 (API 제안: {}초)",
                                        retrySignal.totalRetries() + 1,
                                        maxRetryAttempts,
                                        retryDelaySeconds * (retrySignal.totalRetries() + 1),
                                        suggestedDelay);
                            } else if (failure instanceof AiException) {
                                log.warn("Gemini API 안전 필터 차단 (NO_IMAGE) - Retry {}/{}, {}초 후 재시도",
                                        retrySignal.totalRetries() + 1,
                                        maxRetryAttempts,
                                        retryDelaySeconds * (retrySignal.totalRetries() + 1));
                            }
                        })
                )
                .onErrorResume(throwable -> {
                    if (throwable instanceof AiException) {
                        return Mono.error(throwable);
                    }
                    log.error("Gemini API 호출 실패 - Placeholder 이미지 반환", throwable);
                    return Mono.just(imageProcessingService.generatePlaceholderImage());
                })
                .block();
    }

    private byte[] requestImage(String base64Image, String mimeType, String prompt) throws Exception {
        GeminiImageRequest request = GeminiImageRequest.createImageEdit(base64Image, mimeType, prompt);

        log.info("Gemini API 요청 준비 완료 - URL: {}, Base64 이미지 길이: {}, MIME: {}, 프롬프트 길이: {}",
                geminiImageUrl, base64Image != null ? base64Image.length() : 0, mimeType, prompt.length());

        String rawResponse = webClient.post()
                .uri(geminiImageUrl)
                .header("x-goog-api-key", geminiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Gemini API 에러 응답 (HTTP 에러): {}", errorBody);
                                    if (errorBody.contains("RATE_LIMIT_EXCEEDED")
                                            || errorBody.contains("RESOURCE_EXHAUSTED")
                                            || errorBody.toLowerCase().contains("quota")) {

                                        Integer retryDelay = parseRetryDelay(errorBody);
                                        return Mono.error(new AiQuotaExceededException(
                                                "API Quota 초과: " + errorBody,
                                                "RATE_LIMIT",
                                                retryDelay
                                        ));
                                    }
                                    return Mono.error(new RuntimeException("Gemini API 호출 실패: " + errorBody));
                                })
                )
                .bodyToMono(String.class)
                .block();

        log.info("Gemini API 원본 응답 (처음 500자): {}",
                rawResponse != null ? rawResponse.substring(0, Math.min(500, rawResponse.length())) : "null");

        GeminiImageResponse response = objectMapper.readValue(rawResponse, GeminiImageResponse.class);
        if (response == null) {
            log.error("Gemini API 응답이 null입니다");
            throw new AiException(AiErrorCode.EMPTY_RESPONSE);
        }

        String textMessage = response.getTextMessage();
        if (textMessage != null && !textMessage.isEmpty()) {
            log.warn("Gemini API 응답 텍스트: {}", textMessage);
        }

        // NO_IMAGE 응답 처리 (안전 필터 차단)
        String finishReason = response.getFinishReason();
        if (response.isBlockedByFilter()) {
            log.warn("Gemini API 이미지 생성 차단 - finishReason: {}, 텍스트 메시지: {}", finishReason, textMessage);
            throw new AiException(AiErrorCode.IMAGE_BLOCKED_BY_FILTER);
        }

        if (!response.hasImage()) {
            log.error("Gemini API에서 이미지가 반환되지 않음. finishReason: {}, 텍스트 메시지: {}", finishReason, textMessage);
            throw new AiException(AiErrorCode.EMPTY_RESPONSE);
        }

        String base64ImageResult = response.getFirstImageBase64();
        if (base64ImageResult == null || base64ImageResult.isEmpty()) {
            log.error("Gemini API 응답에서 Base64 이미지 데이터를 찾을 수 없음");
            throw new AiException(AiErrorCode.EMPTY_RESPONSE);
        }

        byte[] result = Base64.getDecoder().decode(base64ImageResult);
        log.info("Gemini Image Edit API 호출 성공 - 이미지 크기: {} bytes", result.length);
        return result;
    }

    private Integer parseRetryDelay(String errorBody) {
        if (errorBody == null) {
            return null;
        }

        try {
            int retryDelayIndex = errorBody.indexOf("\"retryDelay\":");
            if (retryDelayIndex == -1) {
                return null;
            }

            int quoteIndex = errorBody.indexOf("\"", retryDelayIndex + 13);
            int startIndex = quoteIndex + 1;
            int endIndex = errorBody.indexOf("s\"", startIndex);

            if (quoteIndex < 0 || startIndex <= 0 || endIndex <= startIndex) {
                return null;
            }

            String delayStr = errorBody.substring(startIndex, endIndex);
            return Integer.parseInt(delayStr);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            log.warn("재시도 대기 시간 파싱 실패 - errorBody: {}", errorBody, e);
            return null;
        }
    }
}
