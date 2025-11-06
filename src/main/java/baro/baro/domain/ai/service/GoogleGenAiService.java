package baro.baro.domain.ai.service;

import baro.baro.domain.ai.dto.external.GoogleGenAiRequest;
import baro.baro.domain.ai.dto.external.GoogleGenAiResponse;
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
 *   <li>성장/노화 이미지 생성: 현재, +5년, +10년 총 3장</li>
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

    @Value("${google.genai.api.key:}")
    private String apiKey;

    @Value("${google.genai.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent}")
    private String apiUrl;

    /**
     * 성장/노화 이미지 3장 생성
     *
     * <p>실종자의 현재 나이를 기준으로 3개의 성장/노화 이미지를 생성합니다.</p>
     *
     * <p><b>생성 이미지:</b></p>
     * <ul>
     *   <li>0번: 현재 나이</li>
     *   <li>1번: +5년 후</li>
     *   <li>2번: +10년 후</li>
     * </ul>
     *
     * @param missingPerson 실종자 정보
     * @return 생성된 이미지 URL 리스트 (3개)
     */
    public List<String> generateAgeProgressionImages(MissingPerson missingPerson) {
        log.info("성장/노화 이미지 생성 시작 - MissingPerson ID: {}", missingPerson.getId());
        
        List<String> imageUrls = new ArrayList<>();
        
        // 1. 현재 나이 기준
        String currentAgePrompt = buildAgeProgressionPrompt(missingPerson, 0);
        imageUrls.add(generateImage(currentAgePrompt, 0));
        
        // 2. +5년 후
        String fiveYearsPrompt = buildAgeProgressionPrompt(missingPerson, 5);
        imageUrls.add(generateImage(fiveYearsPrompt, 1));
        
        // 3. +10년 후
        String tenYearsPrompt = buildAgeProgressionPrompt(missingPerson, 10);
        imageUrls.add(generateImage(tenYearsPrompt, 2));
        
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
     * <p>실종자 정보를 기반으로 특정 연령대의 초상화를 생성하기 위한 프롬프트를 구성합니다.</p>
     *
     * @param person 실종자 정보
     * @param yearsAhead 현재 나이 기준으로 몇 년 후인지 (0, 5, 10)
     * @return 영문 프롬프트 텍스트
     */
    private String buildAgeProgressionPrompt(MissingPerson person, int yearsAhead) {
        Integer currentAge = person.getAge();
        int targetAge = currentAge != null ? currentAge + yearsAhead : yearsAhead;
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a realistic portrait of a person with the following characteristics:\n");
        prompt.append("- Age: ").append(targetAge).append(" years old\n");
        prompt.append("- Gender: ").append(person.getGender() != null ? person.getGender().name() : "Unknown").append("\n");
        
        if (person.getHeight() != null) {
            prompt.append("- Height: approximately ").append(person.getHeight()).append(" cm\n");
        }
        
        if (person.getBody() != null && !person.getBody().isEmpty()) {
            prompt.append("- Physical description: ").append(person.getBody()).append("\n");
        }
        
        if (person.getBodyEtc() != null && !person.getBodyEtc().isEmpty()) {
            prompt.append("- Additional features: ").append(person.getBodyEtc()).append("\n");
        }
        
        prompt.append("\nStyle: Photorealistic, high quality, portrait photography");
        
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
        
        if (person.getBody() != null && !person.getBody().isEmpty()) {
            prompt.append("- Physical description: ").append(person.getBody()).append("\n");
        }
        
        prompt.append("\nStyle: Photorealistic, high quality, full-body portrait");
        
        return prompt.toString();
    }
    
    /**
     * Google GenAI API를 통한 이미지 생성
     *
     * <p>주어진 프롬프트로 Google GenAI API를 호출하여 이미지를 생성합니다.
     * 현재는 Mock URL을 반환하며, 추후 실제 이미지 생성 및 업로드 로직이 추가되어야 합니다.</p>
     *
     * <p><b>TODO:</b></p>
     * <ul>
     *   <li>nanobanana를 통한 실제 이미지 생성</li>
     *   <li>생성된 이미지를 스토리지에 업로드</li>
     *   <li>업로드된 이미지의 실제 URL 반환</li>
     * </ul>
     *
     * @param prompt 이미지 생성 프롬프트
     * @param sequenceOrder 순서 (0, 1, 2)
     * @return 생성된 이미지 URL (현재는 Mock)
     */
    private String generateImage(String prompt, int sequenceOrder) {
        log.info("이미지 생성 요청 - Prompt: {}", prompt.substring(0, Math.min(100, prompt.length())));

        try {
            // Google GenAI API 호출 (텍스트 생성)
            String generatedText = callGoogleGenAiApi(prompt);

            // TODO: nanobanana를 통한 실제 이미지 생성 및 업로드 구현
            // 현재는 Mock URL 반환
            String mockUrl = "https://storage.googleapis.com/baro-ai-assets/generated-" +
                    UUID.randomUUID().toString() + ".jpg";

            log.info("이미지 생성 완료 - URL: {}", mockUrl);
            return mockUrl;

        } catch (Exception e) {
            log.error("이미지 생성 실패", e);
            // 실패 시 Mock URL 반환 (개발 단계)
            String fallbackUrl = "https://storage.googleapis.com/baro-ai-assets/fallback-" +
                    UUID.randomUUID().toString() + ".jpg";
            log.warn("Fallback 이미지 URL 반환: {}", fallbackUrl);
            return fallbackUrl;
        }
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
