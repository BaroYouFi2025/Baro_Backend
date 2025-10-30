package baro.baro.domain.ai.service;


import baro.baro.domain.ai.dto.external.ImagenRequest;
import baro.baro.domain.ai.dto.external.ImagenResponse;
import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.exception.AiQuotaExceededException;
import baro.baro.domain.missingperson.entity.MissingPerson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Google GenAI 이미지 생성 서비스
 *
 * <p>Google Generative AI (Gemini) API를 활용하여 실종자 정보 기반의
 * AI 이미지를 생성하는 서비스입니다. WebClient를 사용하여 비동기 HTTP 통신을 수행합니다.</p>
 *
 * <p><b>주요 기능:</b></p>
 * <ul>
 *   <li>성장/노화 이미지 생성: 실종 당시 사진 기반 현재 나이 예측 (3가지 스타일/각도)</li>
 *   <li>인상착의 기반 이미지 생성: 의상 및 외모 정보 기반 1장</li>
 *   <li>WebClient를 통한 Google GenAI API 호출</li>
 * </ul>
 *
 * <p><b>환경 변수:</b></p>
 * <ul>
 *   <li>google.genai.api.key: Google GenAI API 키</li>
 *   <li>google.genai.api.url: API 엔드포인트 URL</li>
 * </ul>
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleGenAiService {

    private final WebClient webClient;
    private final baro.baro.domain.image.service.ImageService imageService;
    private final RateLimiter rateLimiter;

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

    /**
     * 성장/노화 이미지 3장 생성
     *
     * <p>실종 당시 어린 시절 사진을 기반으로 현재 나이의 얼굴을 3개의 예측한 이미지를 생성합니다.</p>
     * @param missingPerson 실종자 정보
     * @return 생성된 이미지 URL 리스트 (3개)
     */
    public List<String> generateAgeProgressionImages(MissingPerson missingPerson) {
        if (missingPerson == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_NOT_FOUND);
        }
        
        log.info("성장/노화 이미지 생성 시작 - MissingPerson ID: {}, 실종 당시 나이: {}, 현재 나이: {}",
                missingPerson.getId(), missingPerson.getMissingAge(), missingPerson.getAge());

        List<String> imageUrls = new ArrayList<>();

        String prompt = buildAgeProgressionPrompt(missingPerson);
        for (int styleVariant = 0; styleVariant < 3; styleVariant++) {
            imageUrls.add(generateImage(prompt, styleVariant));
        }

        log.info("성장/노화 이미지 생성 완료 - 총 {}장", imageUrls.size());
        return imageUrls;
    }
    
    /**
     * 인상착의 기반 이미지 1장 생성
     *
     * <p>실종자의 인상착의 정보(의상, 외모 등)를 기반으로 전신 이미지를 생성합니다.</p>
     *
     * <p><b>사용 정보:</b></p>
     * <ul>
     *   <li>이름, 나이, 성별</li>
     *   <li>키, 몸무게</li>
     *   <li>상의, 하의, 기타 의상</li>
     *   <li>신체 특징 및 기타 정보</li>
     * </ul>
     *
     * @param missingPerson 실종자 정보
     * @return 생성된 이미지 URL
     */
    public String generateDescriptionImage(MissingPerson missingPerson) {
        if (missingPerson == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_NOT_FOUND);
        }
        
        log.info("인상착의 이미지 생성 시작 - MissingPerson ID: {}", missingPerson.getId());
        
        String prompt = buildDescriptionPrompt(missingPerson);
        String imageUrl = generateImage(prompt, 0);
        
        log.info("인상착의 이미지 생성 완료 - URL: {}", imageUrl);
        return imageUrl;
    }
    
    /**
     * 성장/노화 프롬프트 생성
     *
     * <p>실종 당시 어린 시절 사진을 기반으로 현재 나이로 성장한 모습의 초상화를 생성하기 위한 프롬프트를 구성합니다.</p>
     *
     * @param person 실종자 정보
     * @return 영문 프롬프트 텍스트
     */
    private String buildAgeProgressionPrompt(MissingPerson person) {
        Integer missingAge = person.getMissingAge();
        Integer currentAge = person.getAge();

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a realistic age progression portrait showing how a person would look now.\n");
        prompt.append("Context: This person went missing at age ").append(missingAge != null ? missingAge : "unknown");
        prompt.append(" and would now be approximately ").append(currentAge != null ? currentAge : "unknown").append(" years old.\n\n");

        prompt.append("Person characteristics:\n");
        prompt.append("- Current age: ").append(currentAge != null ? currentAge : "Unknown").append(" years old\n");
        prompt.append("- Gender: ").append(person.getGender() != null ? person.getGender().name() : "Unknown").append("\n");

        if (person.getHeight() != null) {
            prompt.append("- Height: approximately ").append(person.getHeight()).append(" cm\n");
        }

        if (person.getDescription() != null && !person.getDescription().isEmpty()) {
            prompt.append("- Physical description: ").append(person.getDescription()).append("\n");
        }

        if (person.getBodyEtc() != null && !person.getBodyEtc().isEmpty()) {
            prompt.append("- Additional features: ").append(person.getBodyEtc()).append("\n");
        }

        String styleInstruction = "Front-facing portrait, neutral expression, clear facial features";

        prompt.append("\nStyle: Photorealistic, high quality, age-progressed portrait. ");
        prompt.append(styleInstruction);

        return prompt.toString();
    }
    
    /**
     * 인상착의 프롬프트 생성
     *
     * <p>실종자의 의상, 외모 정보를 기반으로 전신 이미지를 생성하기 위한 프롬프트를 구성합니다.</p>
     *
     * @param person 실종자 정보
     * @return 영문 프롬프트 텍스트
     */
    private String buildDescriptionPrompt(MissingPerson person) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a realistic full-body portrait of a person based on the following description:\n");
        prompt.append("- Name: ").append(person.getName()).append("\n");
        prompt.append("- Age: ").append(person.getAge() != null ? person.getAge() : "Unknown").append(" years old\n");
        prompt.append("- Gender: ").append(person.getGender() != null ? person.getGender().name() : "Unknown").append("\n");
        
        if (person.getHeight() != null) {
            prompt.append("- Height: ").append(person.getHeight()).append(" cm\n");
        }
        
        if (person.getWeight() != null) {
            prompt.append("- Weight: ").append(person.getWeight()).append(" kg\n");
        }
        
        if (person.getClothesTop() != null && !person.getClothesTop().isEmpty()) {
            prompt.append("- Top clothing: ").append(person.getClothesTop()).append("\n");
        }
        
        if (person.getClothesBottom() != null && !person.getClothesBottom().isEmpty()) {
            prompt.append("- Bottom clothing: ").append(person.getClothesBottom()).append("\n");
        }
        
        if (person.getClothesEtc() != null && !person.getClothesEtc().isEmpty()) {
            prompt.append("- Additional clothing: ").append(person.getClothesEtc()).append("\n");
        }
        
        if (person.getDescription() != null && !person.getDescription().isEmpty()) {
            prompt.append("- Physical description: ").append(person.getDescription()).append("\n");
        }
        
        prompt.append("\nStyle: Photorealistic, high quality, full-body portrait");
        
        return prompt.toString();
    }
    
    /**
     * 이미지 생성 (Gemini Image API 사용)
     *
     * <p>주어진 프롬프트로 이미지를 생성하고 로컬 스토리지에 저장합니다.</p>
     *
     * <p><b>처리 흐름:</b></p>
     * <ol>
     *   <li>Google Gemini Image API를 통한 실제 이미지 생성</li>
     *   <li>Base64 이미지를 byte[]로 디코딩</li>
     *   <li>ImageService를 통해 로컬 스토리지에 저장</li>
     *   <li>저장된 이미지의 접근 가능한 URL 반환</li>
     * </ol>
     *
     * @param prompt 이미지 생성 프롬프트
     * @param sequenceOrder 순서 (0, 1, 2)
     * @return 생성된 이미지 URL
     */
    private String generateImage(String prompt, int sequenceOrder) {
        log.info("이미지 생성 요청 - Sequence: {}, Prompt: {}",
                sequenceOrder, prompt.substring(0, Math.min(100, prompt.length())));

        try {
            // 1. Gemini Image API로 실제 이미지 생성
            byte[] imageData = callGeminiImageApi(prompt);

            // 2. ImageService를 통해 저장
            String filename = String.format("ai-generated-%s.jpg", UUID.randomUUID());
            String imageUrl = imageService.saveImageFromBytes(imageData, filename, "image/jpeg");

            log.info("이미지 생성 완료 - URL: {}", imageUrl);
            return imageUrl;

        } catch (AiException e) {
            throw e; // AiException은 그대로 전파
        } catch (Exception e) {
            log.error("이미지 생성 실패 - ", e);

            // 실패 시 Fallback 이미지 생성
            try {
                byte[] fallbackImage = generatePlaceholderImage();
                String filename = String.format("ai-fallback-%s.jpg", UUID.randomUUID());
                String fallbackUrl = imageService.saveImageFromBytes(fallbackImage, filename, "image/jpeg");
                log.warn("Fallback 이미지 URL 반환: {}", fallbackUrl);
                return fallbackUrl;
            } catch (Exception fallbackException) {
                log.error("Fallback 이미지 생성도 실패", fallbackException);
                throw new AiException(AiErrorCode.IMAGE_SAVE_FAILED, fallbackException);
            }
        }
    }

    /**
     * Imagen API를 통한 실제 이미지 생성
     *
     * <p>Google Imagen 3.0 API를 호출하여 텍스트 프롬프트 기반으로
     * 실제 AI 생성 이미지를 만들고 byte[] 데이터로 반환합니다.</p>
     *
     * <p><b>처리 흐름:</b></p>
     * <ol>
     *   <li>Rate Limiting 체크</li>
     *   <li>Imagen API 호출 (predict 엔드포인트)</li>
     *   <li>Base64 이미지 응답 디코딩</li>
     *   <li>에러 시 재시도 (최대 3회)</li>
     *   <li>실패 시 placeholder 이미지 반환</li>
     * </ol>
     *
     * @param prompt 이미지 생성 프롬프트
     * @return 생성된 이미지의 byte[] 데이터
     * @throws RuntimeException API 호출 실패 또는 이미지 생성 실패 시
     */
    private byte[] callGeminiImageApi(String prompt) {
        log.info("Imagen API 호출 시작 - Prompt: {}", prompt.substring(0, Math.min(50, prompt.length())));

        // API Key가 설정되지 않은 경우 Placeholder 반환
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            log.warn("Gemini API Key가 설정되지 않음 - Placeholder 이미지 반환");
            return generatePlaceholderImage();
        }

        // Rate limit 초과 시 placeholder 반환
        if (quotaCheckEnabled) {
            if (!rateLimiter.tryAcquire(quotaRpm, quotaRpd)) {
                long waitTime = rateLimiter.getWaitTimeSeconds(quotaRpm);
                log.error("Rate Limit 초과 - 현재 RPM: {}/{}, RPD: {}/{}, 대기 시간: {}초",
                        rateLimiter.getCurrentRpm(), quotaRpm,
                        rateLimiter.getCurrentRpd(), quotaRpd,
                        waitTime);
                
                // Rate limit 초과 시 placeholder 반환
                return generatePlaceholderImage();
            }
            
            log.debug("Rate Limit 체크 통과 - 현재 RPM: {}/{}, RPD: {}/{}",
                    rateLimiter.getCurrentRpm(), quotaRpm,
                    rateLimiter.getCurrentRpd(), quotaRpd);
        }

        int maxRetries = maxRetryAttempts;
        //
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Imagen API 요청 DTO 생성 (1:1 비율 이미지 1장)
                ImagenRequest request = ImagenRequest.create(prompt);

                // WebClient로 Imagen API 호출
                ImagenResponse response = webClient.post()
                        .uri(geminiImageUrl)
                        .header("x-goog-api-key", geminiApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(
                                status -> status.is4xxClientError() || status.is5xxServerError(),
                                clientResponse -> clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.error("Imagen API 에러 응답: {}", errorBody);
                                            
                                            // Quota 초과 에러인지 확인
                                            if (errorBody.contains("RATE_LIMIT_EXCEEDED") || 
                                                errorBody.contains("RESOURCE_EXHAUSTED") ||
                                                errorBody.toLowerCase().contains("quota")) {
                                                
                                                Integer retryDelay = parseRetryDelay(errorBody);
                                                return Mono.error(new AiQuotaExceededException(
                                                    "API Quota 초과: " + errorBody, 
                                                    "RATE_LIMIT", 
                                                    retryDelay
                                                ));
                                            }
                                            
                                            // 일반 에러
                                            return Mono.error(new RuntimeException("Imagen API 호출 실패: " + errorBody));
                                        })
                        )
                        .bodyToMono(ImagenResponse.class)
                        .block();

                // 응답 검증 및 이미지 데이터 추출
                if (response == null) {
                    throw new AiException(AiErrorCode.EMPTY_RESPONSE, "Imagen API 응답이 null");
                }

                // Base64 이미지 데이터를 byte[]로 디코딩
                String base64Image = response.getFirstImageBase64();
                if (base64Image == null || base64Image.isEmpty()) {
                    throw new AiException(AiErrorCode.EMPTY_RESPONSE, "생성된 이미지 데이터가 없습니다");
                }

                byte[] imageData = java.util.Base64.getDecoder().decode(base64Image);
                log.info("Imagen API 호출 성공 - 이미지 크기: {} bytes", imageData.length);

                return imageData;

            } catch (AiQuotaExceededException e) {
                log.warn("Imagen API Quota 초과 - Attempt {}/{}", attempt, maxRetries);
                
                // 마지막 시도가 아니면 재시도 대기
                if (attempt < maxRetries) {
                    // API 응답에서 제안한 재시도 시간 사용 또는 Exponential backoff
                    int waitTime = e.getRetryAfterSeconds() != null 
                        ? e.getRetryAfterSeconds() 
                        : retryDelaySeconds * attempt;
                    log.info("{}초 후 재시도...", waitTime);
                    
                    try {
                        Thread.sleep(waitTime * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                } else {
                    log.error("최대 재시도 횟수 초과 - Placeholder 이미지 반환");
                    return generatePlaceholderImage();
                }
                
            } catch (AiException e) {
                throw e; // AiException은 그대로 전파 (재시도 안 함)
            } catch (Exception e) {
                log.error("Imagen API 호출 중 오류 발생 - Attempt {}/{}", attempt, maxRetries, e);
                
                // 일반 예외는 재시도하지 않음
                if (attempt >= maxRetries) {
                    throw new AiException(AiErrorCode.GEMINI_API_ERROR, e);
                }
            }
        }

        // 모든 재시도 실패 시 Placeholder 반환
        log.warn("모든 재시도 실패 - Placeholder 이미지 반환");
        return generatePlaceholderImage();
    }

    /**
     * API 응답에서 재시도 대기 시간 파싱
     *
     * @param errorBody API 에러 응답 본문
     * @return 재시도 대기 시간 (초), 파싱 실패 시 null
     */
    private Integer parseRetryDelay(String errorBody) {
        try {
            // "retryDelay": "40s" 형식 파싱
            int retryDelayIndex = errorBody.indexOf("\"retryDelay\":");
            if (retryDelayIndex == -1) {
                return null;
            }
            
            int startIndex = errorBody.indexOf("\"", retryDelayIndex + 13) + 1;
            int endIndex = errorBody.indexOf("s\"", startIndex);
            
            if (startIndex > 0 && endIndex > startIndex) {
                String delayStr = errorBody.substring(startIndex, endIndex);
                return Integer.parseInt(delayStr);
            }
        } catch (Exception e) {
            log.warn("재시도 대기 시간 파싱 실패", e);
        }
        return null;
    }

    /**
     * Placeholder 이미지 - 1x1 픽셀 JPEG 생성
     * @return 이미지 바이너리 데이터
     */
    private byte[] generatePlaceholderImage() {
        String base64Image = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCXABmAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//2Q==";
        return java.util.Base64.getDecoder().decode(base64Image);
    }


}
