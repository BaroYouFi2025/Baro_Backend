package baro.baro.domain.ai.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Google Generative AI API 응답 DTO
 * Google Gemini API로부터 받은 응답 데이터 구조를 정의합니다.
 * 생성된 텍스트, 안전성 등급, 완료 사유 등의 정보를 포함합니다.
 */
@Data
public class GoogleGenAiResponse {

    /**
     * 후보 응답 리스트
     * AI 모델이 생성한 여러 후보 응답들을 포함합니다.
     */
    @JsonProperty("candidates")
    private List<Candidate> candidates;

    /**
     * 프롬프트 피드백
     * 입력 프롬프트에 대한 안전성 검사 결과를 포함합니다.
     */
    @JsonProperty("promptFeedback")
    private PromptFeedback promptFeedback;

    /**
     * Candidate 클래스
     * AI 모델이 생성한 개별 후보 응답을 나타냅니다.
     */
    @Data
    public static class Candidate {
        /**
         * 생성된 콘텐츠
         * AI가 생성한 텍스트 또는 기타 콘텐츠를 포함합니다.
         */
        @JsonProperty("content")
        private Content content;

        /**
         * 완료 사유
         * 생성이 완료된 이유 (STOP, MAX_TOKENS, SAFETY 등)
         */
        @JsonProperty("finishReason")
        private String finishReason;

        /**
         * 후보 인덱스
         * 여러 후보 중 현재 후보의 순서
         */
        @JsonProperty("index")
        private Integer index;

        /**
         * 안전성 등급 리스트
         * 생성된 콘텐츠의 안전성 평가 결과
         */
        @JsonProperty("safetyRatings")
        private List<SafetyRating> safetyRatings;
    }

    /**
     * Content 클래스
     * 생성된 콘텐츠의 구조를 나타냅니다.
     */
    @Data
    public static class Content {
        /**
         * Part 리스트
         * 텍스트, 이미지 등 여러 종류의 생성된 데이터를 포함합니다.
         */
        @JsonProperty("parts")
        private List<Part> parts;

        /**
         * 역할
         * 콘텐츠 생성자의 역할 (model, user 등)
         */
        @JsonProperty("role")
        private String role;
    }

    /**
     * Part 클래스
     * 생성된 콘텐츠의 개별 부분을 나타냅니다.
     */
    @Data
    public static class Part {
        /**
         * 생성된 텍스트
         * AI 모델이 생성한 텍스트 콘텐츠
         */
        @JsonProperty("text")
        private String text;
    }

    /**
     * SafetyRating 클래스
     * 콘텐츠의 안전성 평가 정보를 나타냅니다.
     */
    @Data
    public static class SafetyRating {
        /**
         * 안전성 카테고리
         * HARM_CATEGORY_HARASSMENT, HARM_CATEGORY_HATE_SPEECH 등
         */
        @JsonProperty("category")
        private String category;

        /**
         * 위험 확률
         * NEGLIGIBLE, LOW, MEDIUM, HIGH 등의 값
         */
        @JsonProperty("probability")
        private String probability;
    }

    /**
     * PromptFeedback 클래스
     * 입력 프롬프트에 대한 피드백 정보를 나타냅니다.
     */
    @Data
    public static class PromptFeedback {
        /**
         * 프롬프트의 안전성 등급 리스트
         * 입력 프롬프트에 대한 안전성 평가 결과
         */
        @JsonProperty("safetyRatings")
        private List<SafetyRating> safetyRatings;
    }

    /**
     * 응답에서 생성된 텍스트 추출
     *
     * <p>API 응답의 첫 번째 후보에서 생성된 텍스트를 추출합니다.
     * 응답이 비어있거나 null인 경우 null을 반환합니다.</p>
     *
     * @return 생성된 텍스트, 없으면 null
     */
    public String getGeneratedText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Candidate firstCandidate = candidates.get(0);
        if (firstCandidate.getContent() == null ||
            firstCandidate.getContent().getParts() == null ||
            firstCandidate.getContent().getParts().isEmpty()) {
            return null;
        }

        return firstCandidate.getContent().getParts().get(0).getText();
    }
}
