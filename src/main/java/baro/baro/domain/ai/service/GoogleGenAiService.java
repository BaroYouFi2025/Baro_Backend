package baro.baro.domain.ai.service;


import baro.baro.domain.ai.dto.external.GeminiImageRequest;
import baro.baro.domain.ai.dto.external.GeminiImageResponse;
import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.exception.AiQuotaExceededException;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.missingperson.entity.MissingPerson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

// Google GenAI 이미지 생성 서비스
// Google Generative AI (Gemini) API를 활용하여 실종자 정보 기반의 AI 이미지를 생성하는 서비스
// WebClient를 사용하여 비동기 HTTP 통신을 수행
//
// 주요 기능:
// - 성장/노화 이미지 생성: 실종 당시 사진 기반 현재 나이 예측 (4개 독립 이미지)
// - 인상착의 기반 이미지 생성: 의상 및 외모 정보 기반 1장
// - WebClient를 통한 Google GenAI API 호출
//
// 환경 변수:
// - google.genai.api.key: Google GenAI API 키
// - google.genai.api.url: API 엔드포인트 URL
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleGenAiService {

    private static final String ASSET_TYPE_AGE_PROGRESSION = "AGE_PROGRESSION";
    private static final String ASSET_TYPE_DESCRIPTION = "DESCRIPTION_IMAGE";

    private final WebClient webClient;
    private final baro.baro.domain.image.service.ImageService imageService;
    private final RateLimiter rateLimiter;
    private final MetricsService metricsService;

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

    @Value("${file.upload-dir:/uploads}")
    private String uploadDir;

    // 성장/노화 이미지 4장 생성 (각각 독립적인 이미지 파일)
    // 4번의 Gemini API 요청을 병렬로 실행하여 각각 독립적인 이미지 파일로 저장
    public List<String> generateAgeProgressionImages(MissingPerson missingPerson) {
        if (missingPerson == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_NOT_FOUND);
        }

        // photo_url 검증
        if (missingPerson.getPhotoUrl() == null || missingPerson.getPhotoUrl().isEmpty()) {
            log.error("성장/노화 이미지 생성 실패 - photo_url이 없습니다. MissingPerson ID: {}", missingPerson.getId());
            throw new AiException(AiErrorCode.PHOTO_URL_REQUIRED);
        }

        log.info("성장/노화 이미지 생성 시작 (4개 독립 이미지) - MissingPerson ID: {}, 실종 당시 나이: {}, 현재 나이: {}, Photo URL: {}",
                missingPerson.getId(), missingPerson.getMissingAge(), missingPerson.getAge(), missingPerson.getPhotoUrl());

        // photo_url에서 이미지 파일 읽기 및 Base64 인코딩 (1회만 수행)
        String base64Image = loadImageAsBase64(missingPerson.getPhotoUrl());
        String mimeType = detectMimeType(missingPerson.getPhotoUrl());

        // 기본 프롬프트 생성
        String basePrompt = buildAgeProgressionPrompt(missingPerson, "Front-facing portrait, looking directly at camera");

        // 4개의 API 요청을 병렬로 처리하기 위한 CompletableFuture 리스트
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            final int sequenceOrder = i;
            CompletableFuture<String> future =
                CompletableFuture.supplyAsync(() -> {
                    log.info("이미지 생성 시작 (Sequence: {})", sequenceOrder);
                    return generateImageWithPhoto(base64Image, mimeType, basePrompt, sequenceOrder, ASSET_TYPE_AGE_PROGRESSION);
                });
            futures.add(future);
        }

        // 모든 비동기 작업 완료 대기 및 결과 수집
        // exceptionally()를 사용하여 실패한 future를 명시적으로 처리
        List<String> imageUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < futures.size(); i++) {
            final int sequenceOrder = i;
            try {
                String url = futures.get(i).join();
                imageUrls.add(url);
                log.info("이미지 생성 성공 (Sequence: {})", sequenceOrder);
            } catch (Exception e) {
                String errorMsg = String.format("Sequence %d 실패: %s", sequenceOrder, e.getMessage());
                errors.add(errorMsg);
                log.error("이미지 생성 실패 (Sequence: {})", sequenceOrder, e);
            }
        }

        // 최소 성공 개수 검증 (4개 중 최소 3개 이상 성공 필요)
        final int MINIMUM_REQUIRED_IMAGES = 3;
        if (imageUrls.size() < MINIMUM_REQUIRED_IMAGES) {
            log.error("AI 이미지 생성 실패: 최소 {}장 필요하나 {}장만 성공. 실패 이유: {}",
                    MINIMUM_REQUIRED_IMAGES, imageUrls.size(), String.join(", ", errors));
            throw new AiException(AiErrorCode.INSUFFICIENT_IMAGES_GENERATED);
        }

        log.info("성장/노화 이미지 생성 완료 - 총 {}장 (요청: 4장, 최소 요구: {}장)",
                imageUrls.size(), MINIMUM_REQUIRED_IMAGES);
        return imageUrls;
    }
    
    // 인상착의 기반 이미지 1장 생성
    // photo_url의 얼굴 이미지 + 인상착의 정보(의상, 체형 등)를 기반으로 전신 이미지를 생성
    // Gemini 이미지 편집 API를 사용하여 얼굴 특징을 유지하면서 전신 모습 생성
    //
    // 사용 정보:
    // - photo_url: 얼굴 이미지
    // - 이름, 나이, 성별
    // - 키, 몸무게
    // - 상의, 하의, 기타 의상
    // - 신체 특징 및 기타 정보
    //
    public String generateDescriptionImage(MissingPerson missingPerson) {
        if (missingPerson == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_NOT_FOUND);
        }

        // photo_url 검증
        if (missingPerson.getPhotoUrl() == null || missingPerson.getPhotoUrl().isEmpty()) {
            log.error("인상착의 이미지 생성 실패 - photo_url이 없습니다. MissingPerson ID: {}", missingPerson.getId());
            throw new AiException(AiErrorCode.PHOTO_URL_REQUIRED);
        }

        log.info("인상착의 이미지 생성 시작 - MissingPerson ID: {}, Photo URL: {}",
                missingPerson.getId(), missingPerson.getPhotoUrl());

        // photo_url에서 이미지 파일 읽기 및 Base64 인코딩
        String base64Image = loadImageAsBase64(missingPerson.getPhotoUrl());
        String mimeType = detectMimeType(missingPerson.getPhotoUrl());

        // 인상착의 프롬프트 생성
        String prompt = buildDescriptionPrompt(missingPerson);
        String imageUrl = generateImageWithPhoto(base64Image, mimeType, prompt, 0, ASSET_TYPE_DESCRIPTION);

        log.info("인상착의 이미지 생성 완료 - URL: {}", imageUrl);
        return imageUrl;
    }
    
    // 성장/노화 프롬프트 생성 (이미지 편집용 - 정면 사진)
    // 입력 이미지(어린 시절/젊은 시절)를 기반으로 현재 나이로 성장/노화시킨 정면 얼굴을 생성하는 프롬프트
    // Gemini Image Edit API용 프롬프트 - 이미지와 함께 전송됨
    private String buildAgeProgressionPrompt(MissingPerson person, String style) {
        Integer missingAge = person.getMissingAge();
        Integer currentAge = person.getAge();

        StringBuilder prompt = new StringBuilder();

        // 이미지 편집 지시문 - 제공된 이미지를 사용하여 나이를 변경
        prompt.append("Using the provided image of a person's face, perform age progression to show how they would look now.\n\n");

        prompt.append("Age progression details:\n");
        prompt.append("- Original age (when photo was taken): ").append(missingAge != null ? missingAge : "unknown").append(" years old\n");
        prompt.append("- Target age (current age): ").append(currentAge != null ? currentAge : "unknown").append(" years old\n");
        prompt.append("- Gender: ").append(person.getGender() != null ? person.getGender().name() : "Unknown").append("\n");

        if (person.getDescription() != null && !person.getDescription().isEmpty()) {
            prompt.append("- Physical description: ").append(person.getDescription()).append("\n");
        }

        if (person.getBodyEtc() != null && !person.getBodyEtc().isEmpty()) {
            prompt.append("- Additional features: ").append(person.getBodyEtc()).append("\n");
        }

        prompt.append("\nInstructions:\n");
        prompt.append("- Keep the facial identity and core features recognizable\n");
        prompt.append("- Add natural aging effects appropriate for the age difference\n");
        prompt.append("- Maintain realistic skin texture, wrinkles, and facial structure changes\n");
        prompt.append("- Keep the background simple and neutral\n");
        prompt.append("- Generate a clear, front-facing portrait photo\n");
        prompt.append("\nStyle: Photorealistic, high quality portrait. ").append(style).append(". Professional headshot quality.");

        return prompt.toString();
    }
    
    // 인상착의 프롬프트 생성 (이미지 편집용)
    // photo_url의 얼굴 이미지 + 인상착의 정보(의상, 체형)를 기반으로 전신 이미지를 생성하는 프롬프트
    // Gemini Image Edit API용 프롬프트 - 얼굴 특징을 유지하면서 전신 모습 생성
    private String buildDescriptionPrompt(MissingPerson person) {
        StringBuilder prompt = new StringBuilder();

        // 이미지 편집 지시문 - 제공된 얼굴 이미지를 사용하여 전신 이미지 생성
        prompt.append("Using the provided image of a person's face, generate a full-body portrait showing this person with the following appearance and clothing:\n\n");

        prompt.append("Person information:\n");
        prompt.append("- Age: ").append(person.getAge() != null ? person.getAge() : "Unknown").append(" years old\n");
        prompt.append("- Gender: ").append(person.getGender() != null ? person.getGender().name() : "Unknown").append("\n");

        if (person.getHeight() != null) {
            prompt.append("- Height: approximately ").append(person.getHeight()).append(" cm\n");
        }

        if (person.getWeight() != null) {
            prompt.append("- Weight: approximately ").append(person.getWeight()).append(" kg\n");
        }

        // 체형 정보
        if (person.getDescription() != null && !person.getDescription().isEmpty()) {
            prompt.append("- Body type: ").append(person.getDescription()).append("\n");
        }

        if (person.getBodyEtc() != null && !person.getBodyEtc().isEmpty()) {
            prompt.append("- Additional physical features: ").append(person.getBodyEtc()).append("\n");
        }

        // 의상 정보
        prompt.append("\nClothing details:\n");

        if (person.getClothesTop() != null && !person.getClothesTop().isEmpty()) {
            prompt.append("- Top: ").append(person.getClothesTop()).append("\n");
        }

        if (person.getClothesBottom() != null && !person.getClothesBottom().isEmpty()) {
            prompt.append("- Bottom: ").append(person.getClothesBottom()).append("\n");
        }

        if (person.getClothesEtc() != null && !person.getClothesEtc().isEmpty()) {
            prompt.append("- Accessories/Other: ").append(person.getClothesEtc()).append("\n");
        }

        prompt.append("\nInstructions:\n");
        prompt.append("- Keep the facial features from the provided image EXACTLY as they are\n");
        prompt.append("- Generate a full-body portrait showing the person standing naturally\n");
        prompt.append("- Apply the clothing and body type described above\n");
        prompt.append("- Use a simple, neutral background\n");
        prompt.append("- Ensure the face matches the input image perfectly\n");

        prompt.append("\nStyle: Photorealistic, high quality, full-body portrait, natural lighting");

        return prompt.toString();
    }
    
    // 이미지 편집/생성 (Gemini Image API 사용 - 이미지 + 프롬프트)
    // 입력 이미지 + 텍스트 프롬프트로 새로운 이미지를 생성하고 로컬 스토리지에 저장
    //
    // 처리 흐름:
    // 1. Google Gemini Image Edit API 호출 (이미지 + 프롬프트)
    // 2. Base64 이미지 응답을 byte[]로 디코딩
    // 3. ImageService를 통해 로컬 스토리지에 저장
    // 4. 저장된 이미지의 접근 가능한 URL 반환
    //
    //          prompt - 편집 지시 프롬프트, sequenceOrder - 순서 (0, 1, 2, 3)
    private String generateImageWithPhoto(String base64Image, String mimeType, String prompt, int sequenceOrder, String assetType) {
        long startTime = System.currentTimeMillis();
        log.info("이미지 편집 요청 - Sequence: {}, AssetType: {}, MIME: {}, Prompt: {}",
                sequenceOrder, assetType, mimeType, prompt.substring(0, Math.min(100, prompt.length())));

        try {
            // 1. Gemini Image Edit API로 이미지 편집/생성
            byte[] imageData = callGeminiImageEditApi(base64Image, mimeType, prompt);

            // 2. ImageService를 통해 저장
            String filename = String.format("ai-generated-%s.png", UUID.randomUUID());
            String imageUrl = imageService.saveImageFromBytes(imageData, filename, "image/png");

            metricsService.recordAiImageGenerationSuccess(assetType);
            metricsService.recordAiGenerationDuration(System.currentTimeMillis() - startTime, assetType);
            log.info("이미지 편집 완료 - URL: {}", imageUrl);
            return imageUrl;

        } catch (AiException e) {
            metricsService.recordAiImageGenerationFailure(assetType, e.getAiErrorCode().name());
            throw e; // AiException은 그대로 전파
        } catch (Exception e) {
            metricsService.recordAiImageGenerationFailure(assetType, e.getClass().getSimpleName());
            log.error("이미지 편집 실패 - ", e);

            // 실패 시 Fallback 이미지 생성
            try {
                byte[] fallbackImage = generatePlaceholderImage();
                String filename = String.format("ai-fallback-%s.jpg", UUID.randomUUID());
                String fallbackUrl = imageService.saveImageFromBytes(fallbackImage, filename, "image/jpeg");
                log.warn("Fallback 이미지 URL 반환: {}", fallbackUrl);
                return fallbackUrl;
            } catch (Exception fallbackException) {
                log.error("Fallback 이미지 생성도 실패", fallbackException);
                throw new AiException(AiErrorCode.IMAGE_SAVE_FAILED);
            }
        }
    }

    // Gemini 이미지 편집 API 호출 (이미지 + 프롬프트)
    // Google Gemini 2.5 Flash Image 모델을 사용하여 이미지 편집/생성
    // 입력 이미지 + 텍스트 프롬프트로 새로운 이미지 생성
    //
    // 처리 흐름:
    // 1. Rate Limiting 체크
    // 2. Gemini Image Edit API 호출 (generateContent 엔드포인트)
    // 3. Base64 이미지 응답 디코딩
    // 4. 에러 시 재시도 (최대 3회)
    // 5. 실패 시 placeholder 이미지 반환
    //
    private byte[] callGeminiImageEditApi(String base64Image, String mimeType, String prompt) {
        log.info("Gemini Image Edit API 호출 시작 - MIME: {}, Prompt: {}",
                mimeType, prompt.substring(0, Math.min(50, prompt.length())));

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

        // Reactive retry with non-blocking delay (Thread.sleep 제거)
        // Mono.fromCallable()로 감싸서 retryWhen() 적용
        byte[] imageData = Mono.fromCallable(() -> {
            // Gemini Image Edit 요청 DTO 생성 (이미지 + 텍스트)
            GeminiImageRequest request = GeminiImageRequest.createImageEdit(base64Image, mimeType, prompt);

            log.info("Gemini API 요청 준비 완료 - URL: {}, Base64 이미지 길이: {}, MIME: {}, 프롬프트 길이: {}",
                    geminiImageUrl, base64Image != null ? base64Image.length() : 0, mimeType, prompt.length());

            // WebClient로 Gemini API 호출
            // 먼저 String으로 응답을 받아서 로깅한 후 파싱
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
                                        return Mono.error(new RuntimeException("Gemini API 호출 실패: " + errorBody));
                                    })
                    )
                    .bodyToMono(String.class)
                    .block();

            log.info("Gemini API 원본 응답 (처음 500자): {}",
                    rawResponse != null ? rawResponse.substring(0, Math.min(500, rawResponse.length())) : "null");

            // JSON을 객체로 파싱
            GeminiImageResponse response;
            try {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                response = objectMapper.readValue(rawResponse, GeminiImageResponse.class);
            } catch (Exception e) {
                log.error("Gemini API 응답 파싱 실패", e);
                throw new AiException(AiErrorCode.INVALID_RESPONSE_FORMAT);
            }

            // 응답 검증 및 이미지 데이터 추출
            if (response == null) {
                log.error("Gemini API 응답이 null입니다");
                throw new AiException(AiErrorCode.EMPTY_RESPONSE);
            }

            log.info("Gemini API 응답 수신 - Candidates 수: {}",
                    response.getCandidates() != null ? response.getCandidates().size() : 0);

            // 텍스트 메시지만 온 경우 (에러/정책 안내)
            String textMessage = response.getTextMessage();
            if (textMessage != null && !textMessage.isEmpty()) {
                log.warn("Gemini API 응답 텍스트: {}", textMessage);
            }

            if (!response.hasImage()) {
                log.error("Gemini API에서 이미지가 반환되지 않음. 텍스트 메시지: {}", textMessage);
                throw new AiException(AiErrorCode.EMPTY_RESPONSE);
            }

            // Base64 이미지 데이터를 byte[]로 디코딩
            String base64ImageResult = response.getFirstImageBase64();
            if (base64ImageResult == null || base64ImageResult.isEmpty()) {
                log.error("Gemini API 응답에서 Base64 이미지 데이터를 찾을 수 없음");
                throw new AiException(AiErrorCode.EMPTY_RESPONSE);
            }

            log.info("Base64 이미지 데이터 추출 완료 - 길이: {}", base64ImageResult.length());

            byte[] result = Base64.getDecoder().decode(base64ImageResult);
            log.info("Gemini Image Edit API 호출 성공 - 이미지 크기: {} bytes", result.length);

            return result;
        })
        .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofSeconds(retryDelaySeconds))
                .filter(throwable -> throwable instanceof AiQuotaExceededException)
                .doBeforeRetry(retrySignal -> {
                    AiQuotaExceededException ex = (AiQuotaExceededException) retrySignal.failure();
                    Integer suggestedDelay = ex.getRetryAfterSeconds();
                    log.warn("Gemini API Quota 초과 - Retry {}/{}, {}초 후 재시도 (API 제안: {}초)",
                            retrySignal.totalRetries() + 1,
                            maxRetryAttempts,
                            retryDelaySeconds * (retrySignal.totalRetries() + 1),
                            suggestedDelay);
                })
        )
        .onErrorResume(throwable -> {
            // AiException은 그대로 전파 (재시도 안 함)
            if (throwable instanceof AiException) {
                return Mono.error(throwable);
            }
            // 최대 재시도 횟수 초과 또는 일반 예외 발생 시 Placeholder 반환
            log.error("API 호출 실패 - Placeholder 이미지 반환", throwable);
            return Mono.just(generatePlaceholderImage());
        })
        .block();

        return imageData;
    }

    // API 응답에서 재시도 대기 시간 파싱
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

    // 이미지 파일을 Base64로 인코딩
    // photo_url에서 이미지를 읽어와 Base64 문자열로 변환
    // URL을 로컬 파일 경로로 변환하여 직접 읽기 (SSL 문제 회피)
    private String loadImageAsBase64(String photoUrl) {
        try {
            byte[] imageBytes;

            log.info("이미지 로드 시작 - Photo URL: {}", photoUrl);

            // URL을 로컬 파일 경로로 변환 시도
            Path imagePath = convertUrlToLocalPath(photoUrl);

            if (imagePath != null) {
                log.info("URL → 로컬 경로 변환 완료: {}", imagePath);

                if (Files.exists(imagePath)) {
                    // 로컬 파일에서 직접 로드
                    log.info("로컬 파일에서 이미지 로드: {}", imagePath);
                    imageBytes = Files.readAllBytes(imagePath);
                } else {
                    log.warn("로컬 파일이 존재하지 않음: {}, URL 접속 시도", imagePath);
                    // 로컬 파일이 없으면 URL로 접속 시도
                    URL url = new URL(photoUrl);
                    try (InputStream inputStream = url.openStream()) {
                        imageBytes = StreamUtils.copyToByteArray(inputStream);
                    }
                }
            }
            // HTTP/HTTPS URL인 경우 (외부 URL)
            else if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                log.info("외부 HTTP URL에서 이미지 로드: {}", photoUrl);
                URL url = new URL(photoUrl);
                try (InputStream inputStream = url.openStream()) {
                    imageBytes = StreamUtils.copyToByteArray(inputStream);
                }
            }
            // 일반 로컬 경로인 경우
            else {
                Path localPath = Paths.get(photoUrl);
                log.info("로컬 파일에서 이미지 로드: {}", localPath);

                if (!Files.exists(localPath)) {
                    log.error("로컬 파일을 찾을 수 없음: {}", localPath);
                    throw new AiException(AiErrorCode.IMAGE_FILE_NOT_FOUND);
                }

                imageBytes = Files.readAllBytes(localPath);
            }

            // Base64 인코딩
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            log.info("이미지 로드 완료 - 크기: {} bytes, Base64 길이: {}", imageBytes.length, base64.length());

            return base64;

        } catch (AiException e) {
            throw e; // AiException은 그대로 전파
        } catch (IOException e) {
            log.error("이미지 로드 실패 - URL: {}", photoUrl, e);
            throw new AiException(AiErrorCode.IMAGE_LOAD_FAILED);
        }
    }

    // URL을 로컬 파일 경로로 변환
    // 서버의 이미지 URL을 실제 로컬 파일 경로로 변환
    // 예: http://localhost:8080/images/2025/11/02/file.jpg → {현재디렉토리}/uploads/images/2025/11/02/file.jpg
    private Path convertUrlToLocalPath(String photoUrl) {
        try {
            // URL이 아닌 경우
            if (!photoUrl.startsWith("http://") && !photoUrl.startsWith("https://")) {
                return null;
            }

            // URL에서 경로 부분 추출 (예: /images/2025/11/02/file.jpg)
            String path = photoUrl.substring(photoUrl.indexOf("/", 8)); // "http://" 이후의 첫 / 찾기

            // "/images/" 경로를 로컬 경로로 변환
            if (path.startsWith("/images/")) {
                // /images/2025/11/02/file.jpg → uploads/images/2025/11/02/file.jpg
                String relativePath = path.substring(1); // 앞의 / 제거

                // 절대 경로 생성: 현재 작업 디렉토리 + uploadDir + relativePath
                Path currentDir = Paths.get(System.getProperty("user.dir"));
                Path localPath = currentDir.resolve(uploadDir).resolve(relativePath);

                log.debug("URL → 로컬 경로 변환: {} → {}", photoUrl, localPath.toAbsolutePath());
                return localPath;
            }

            return null;

        } catch (Exception e) {
            log.warn("URL을 로컬 경로로 변환 실패: {}", photoUrl, e);
            return null;
        }
    }

    // 파일 확장자로부터 MIME 타입 검출
    private String detectMimeType(String photoUrl) {
        String lowerUrl = photoUrl.toLowerCase();

        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.endsWith(".png")) {
            return "image/png";
        } else if (lowerUrl.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerUrl.endsWith(".webp")) {
            return "image/webp";
        } else {
            // 기본값: JPEG
            log.warn("알 수 없는 이미지 확장자 - 기본값(image/jpeg) 사용: {}", photoUrl);
            return "image/jpeg";
        }
    }

    // Placeholder 이미지 - 1x1 픽셀 JPEG 생성
    private byte[] generatePlaceholderImage() {
        String base64Image = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCXABmAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//2Q==";
        return Base64.getDecoder().decode(base64Image);
    }


}
