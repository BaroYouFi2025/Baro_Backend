package baro.baro.domain.ai.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Google Generative AI API 요청 DTO
 * Google Gemini API 호출 시 사용되는 요청 본문 구조를 정의합니다.
 * contents, generationConfig 등을 포함하여 AI 모델의 텍스트 생성을 제어합니다.
 */
@Data
public class GoogleGenAiRequest {

    /**
     * 요청 콘텐츠 리스트
     * AI 모델에 전달할 입력 데이터(텍스트, 이미지 등)를 포함합니다.
     */
    @JsonProperty("contents")
    private List<Content> contents;

    /**
     * 생성 설정
     * 텍스트 생성 시 온도, topK, topP 등의 파라미터를 조정합니다.
     */
    @JsonProperty("generationConfig")
    private GenerationConfig generationConfig;

    /**
     * Content 클래스
     * AI 모델에 전달할 콘텐츠를 나타냅니다.
     */
    @Data
    public static class Content {
        /**
         * Part 리스트
         * 텍스트, 이미지 등 여러 종류의 데이터를 포함할 수 있습니다.
         */
        @JsonProperty("parts")
        private List<Part> parts;
    }

    /**
     * Part 클래스
     * 콘텐츠의 개별 부분(텍스트, 이미지 등)을 나타냅니다.
     */
    @Data
    public static class Part {
        /**
         * 텍스트 콘텐츠
         * AI 모델에 전달할 텍스트 프롬프트입니다.
         */
        @JsonProperty("text")
        private String text;
    }

    /**
     * GenerationConfig 클래스
     * AI 텍스트 생성 파라미터를 설정합니다.
     */
    @Data
    public static class GenerationConfig {
        /**
         * 온도 (Temperature)
         * 0.0 ~ 1.0 사이의 값. 높을수록 더 창의적이고 다양한 결과 생성 (기본값: 0.7)
         */
        @JsonProperty("temperature")
        private Double temperature;

        /**
         * Top-K 샘플링
         * 상위 K개의 토큰만 샘플링 대상으로 고려 (기본값: 40)
         */
        @JsonProperty("topK")
        private Integer topK;

        /**
         * Top-P 샘플링 (Nucleus Sampling)
         * 누적 확률이 P를 초과하지 않는 토큰들만 샘플링 (기본값: 0.95)
         */
        @JsonProperty("topP")
        private Double topP;

        /**
         * 최대 출력 토큰 수
         * 생성할 최대 토큰 개수를 제한 (기본값: 1024)
         */
        @JsonProperty("maxOutputTokens")
        private Integer maxOutputTokens;
    }

    /**
     * 요청 생성 팩토리 메서드
     *
     * <p>주어진 프롬프트로 Google GenAI API 요청 객체를 생성합니다.
     * 기본 설정값으로 temperature=0.7, topK=40, topP=0.95, maxTokens=1024를 사용합니다.</p>
     *
     * @param prompt AI 모델에 전달할 텍스트 프롬프트
     * @return 생성된 GoogleGenAiRequest 객체
     */
    public static GoogleGenAiRequest create(String prompt) {
        GoogleGenAiRequest request = new GoogleGenAiRequest();

        // Part 생성 - 텍스트 프롬프트 설정
        Part part = new Part();
        part.setText(prompt);

        // Content 생성 - Part 리스트 포함
        Content content = new Content();
        content.setParts(List.of(part));

        // GenerationConfig 설정 - AI 생성 파라미터 조정
        GenerationConfig config = new GenerationConfig();
        config.setTemperature(0.7);   // 창의성 수준
        config.setTopK(40);            // 샘플링 범위
        config.setTopP(0.95);          // 누적 확률 임계값
        config.setMaxOutputTokens(1024); // 최대 출력 길이

        request.setContents(List.of(content));
        request.setGenerationConfig(config);

        return request;
    }
}
