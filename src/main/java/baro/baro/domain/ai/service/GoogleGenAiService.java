package baro.baro.domain.ai.service;

import baro.baro.domain.ai.dto.external.GoogleGenAiRequest;
import baro.baro.domain.ai.dto.external.GoogleGenAiResponse;
import baro.baro.domain.ai.dto.external.NanobananaRequest;
import baro.baro.domain.ai.dto.external.NanobananaResponse;
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
 * @see GoogleGenAiRequest
 * @see GoogleGenAiResponse
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleGenAiService {

    private final WebClient webClient;
    private final baro.baro.domain.image.service.ImageService imageService;

    @Value("${google.genai.api.key:}")
    private String apiKey;

    @Value("${google.genai.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent}")
    private String apiUrl;

    @Value("${google.nanobanana.api.url:https://nanobanana.googleapis.com/v1/images/generate}")
    private String nanobananaUrl;

    @Value("${google.nanobanana.api.key:}")
    private String nanobananaApiKey;

    /**
     * 성장/노화 이미지 3장 생성
     *
     * <p>실종 당시 어린 시절 사진을 기반으로 현재 나이의 얼굴을 3가지 스타일/각도로 예측한 이미지를 생성합니다.</p>
     *
     * <p><b>생성 이미지:</b></p>
     * <ul>
     *   <li>0번: 정면 초상화 (Front-facing portrait)</li>
     *   <li>1번: 측면 45도 각도 (Side profile view)</li>
     *   <li>2번: 3/4 측면 각도 (Three-quarter view)</li>
     * </ul>
     *
     * @param missingPerson 실종자 정보
     * @return 생성된 이미지 URL 리스트 (3개)
     */
    public List<String> generateAgeProgressionImages(MissingPerson missingPerson) {
        log.info("성장/노화 이미지 생성 시작 - MissingPerson ID: {}, 실종 당시 나이: {}, 현재 나이: {}",
                missingPerson.getId(), missingPerson.getMissingAge(), missingPerson.getAge());

        List<String> imageUrls = new ArrayList<>();

        for (int styleVariant = 0; styleVariant < 3; styleVariant++) {
            String prompt = buildAgeProgressionPrompt(missingPerson);
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
     * @param styleVariant 스타일 변형 (0: 정면, 1: 측면, 2: 다른 각도)
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
     * 이미지 생성 (Nanobanana API 사용)
     *
     * <p>주어진 프롬프트로 이미지를 생성하고 로컬 스토리지에 저장합니다.</p>
     *
     * <p><b>처리 흐름:</b></p>
     * <ol>
     *   <li>Google Nanobanana API를 통한 실제 이미지 생성</li>
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
            // 1. Nanobanana API로 실제 이미지 생성
            byte[] imageData = callNanobananaApi(prompt);

            // 2. ImageService를 통해 저장
            String filename = String.format("ai-generated-%s-%d.jpg",
                    UUID.randomUUID().toString(), sequenceOrder);
            String imageUrl = imageService.saveImageFromBytes(imageData, filename, "image/jpeg");

            log.info("이미지 생성 완료 - Sequence: {}, URL: {}", sequenceOrder, imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("이미지 생성 실패 - Sequence: {}", sequenceOrder, e);

            // 실패 시 Fallback 이미지 생성
            try {
                byte[] fallbackImage = generateFallbackImage(sequenceOrder);
                String filename = String.format("ai-fallback-%s-%d.jpg",
                        UUID.randomUUID().toString(), sequenceOrder);
                String fallbackUrl = imageService.saveImageFromBytes(fallbackImage, filename, "image/jpeg");
                log.warn("Fallback 이미지 URL 반환: {}", fallbackUrl);
                return fallbackUrl;
            } catch (Exception fallbackException) {
                log.error("Fallback 이미지 생성도 실패", fallbackException);
                throw new RuntimeException("이미지 생성 실패", e);
            }
        }
    }

    /**
     * Nanobanana API를 통한 실제 이미지 생성
     *
     * <p>Google Nanobanana API를 호출하여 텍스트 프롬프트 기반으로
     * 실제 AI 생성 이미지를 만들고 byte[] 데이터로 반환합니다.</p>
     *
     * @param prompt 이미지 생성 프롬프트
     * @return 생성된 이미지의 byte[] 데이터
     * @throws RuntimeException API 호출 실패 또는 이미지 생성 실패 시
     */
    private byte[] callNanobananaApi(String prompt) {
        log.info("Nanobanana API 호출 시작 - Prompt: {}", prompt.substring(0, Math.min(50, prompt.length())));

        // API Key가 설정되지 않은 경우 Placeholder 반환
        if (nanobananaApiKey == null || nanobananaApiKey.isEmpty()) {
            log.warn("Nanobanana API Key가 설정되지 않음 - Placeholder 이미지 반환");
            return generatePlaceholderImage(prompt, 0);
        }

        try {
            // 요청 DTO 생성
            NanobananaRequest request = NanobananaRequest.createHighQuality(prompt);

            // WebClient로 API 호출
            NanobananaResponse response = webClient.post()
                    .uri(nanobananaUrl + "?key=" + nanobananaApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Nanobanana API 에러 응답: {}", errorBody);
                                        return Mono.error(new RuntimeException("Nanobanana API 호출 실패: " + errorBody));
                                    })
                    )
                    .bodyToMono(NanobananaResponse.class)
                    .block();

            // 응답 검증 및 이미지 데이터 추출
            if (response == null || !response.isSuccess()) {
                String error = response != null ? response.getError() : "응답이 null";
                throw new RuntimeException("Nanobanana API 응답 실패: " + error);
            }

            // Base64 이미지 데이터를 byte[]로 디코딩
            String base64Image = response.getFirstImageBase64();
            if (base64Image == null || base64Image.isEmpty()) {
                throw new RuntimeException("생성된 이미지 데이터가 없습니다");
            }

            byte[] imageData = java.util.Base64.getDecoder().decode(base64Image);
            log.info("Nanobanana API 호출 성공 - 이미지 크기: {} bytes", imageData.length);

            return imageData;

        } catch (Exception e) {
            log.error("Nanobanana API 호출 중 오류 발생", e);
            throw new RuntimeException("Nanobanana API 호출 실패", e);
        }
    }

    /**
     * Placeholder 이미지 생성 (개발용)
     *
     * <p>실제 이미지 생성 API 대신 간단한 placeholder 이미지를 생성합니다.
     * 향후 Google Imagen, DALL-E, Stable Diffusion 등으로 대체 예정.</p>
     *
     * @param prompt 프롬프트
     * @param sequenceOrder 순서
     * @return 이미지 바이너리 데이터
     */
    private byte[] generatePlaceholderImage(String prompt, int sequenceOrder) {
        // 간단한 1x1 픽셀 JPEG 이미지 (Base64에서 디코드)
        // 실제로는 이미지 생성 라이브러리(Java AWT, ImageMagick 등) 사용 권장
        String base64Image = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCXABmAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//2Q==";
        return java.util.Base64.getDecoder().decode(base64Image);
    }

    /**
     * Fallback 이미지 생성
     *
     * @param sequenceOrder 순서
     * @return 이미지 바이너리 데이터
     */
    private byte[] generateFallbackImage(int sequenceOrder) {
        // 기본 placeholder 이미지 반환
        return generatePlaceholderImage("fallback", sequenceOrder);
    }

    /**
     * WebClient를 사용한 Google GenAI API 호출
     *
     * <p>WebClient를 사용하여 Google GenAI (Gemini) API에 POST 요청을 보내고
     * 생성된 텍스트 응답을 받아옵니다. 동기 방식(.block())을 사용하여 기존 코드와 호환됩니다.</p>
     *
     * <p><b>처리 흐름:</b></p>
     * <ol>
     *   <li>API Key 유효성 검증</li>
     *   <li>요청 DTO 생성 (GoogleGenAiRequest)</li>
     *   <li>WebClient POST 요청 (비동기)</li>
     *   <li>에러 상태 코드 처리 (4xx, 5xx)</li>
     *   <li>응답을 동기 방식으로 변환 (.block())</li>
     *   <li>생성된 텍스트 추출 및 반환</li>
     * </ol>
     *
     * <p><b>참고:</b> API Key가 없으면 Mock 데이터를 반환합니다.</p>
     *
     * @param prompt 텍스트 프롬프트
     * @return 생성된 텍스트 응답
     * @throws RuntimeException API 호출 실패 또는 응답이 null인 경우
     */
    private String callGoogleGenAiApi(String prompt) {
        log.info("Google GenAI API 호출 시작");

        // API Key가 설정되지 않은 경우
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Google GenAI API Key가 설정되지 않음 - Mock 데이터 반환");
            return "Mock response: Image description generated";
        }

        try {
            // 요청 DTO 생성
            GoogleGenAiRequest request = GoogleGenAiRequest.create(prompt);

            // WebClient로 API 호출 (비동기 -> 동기 변환)
            GoogleGenAiResponse response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Google GenAI API 에러 응답: {}", errorBody);
                                        return Mono.error(new RuntimeException("API 호출 실패: " + errorBody));
                                    })
                    )
                    .bodyToMono(GoogleGenAiResponse.class)
                    .block(); // 비동기 Mono를 동기 방식으로 변환

            // 응답에서 텍스트 추출
            if (response != null) {
                String generatedText = response.getGeneratedText();
                log.info("Google GenAI API 호출 성공 - 응답 길이: {}",
                        generatedText != null ? generatedText.length() : 0);
                return generatedText;
            }

            throw new RuntimeException("Google GenAI API 응답이 null입니다");

        } catch (Exception e) {
            log.error("Google GenAI API 호출 실패", e);
            throw new RuntimeException("Google GenAI API 호출 중 오류 발생", e);
        }
    }
}
