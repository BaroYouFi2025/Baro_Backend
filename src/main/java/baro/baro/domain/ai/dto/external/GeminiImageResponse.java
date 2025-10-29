package baro.baro.domain.ai.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Google Gemini Image Generation API 응답 DTO
 */
@Data
public class GeminiImageResponse {

    @JsonProperty("candidates")
    private List<Candidate> candidates;

    @Data
    public static class Candidate {
        @JsonProperty("content")
        private Content content;

        @JsonProperty("finishReason")
        private String finishReason;
    }

    @Data
    public static class Content {
        @JsonProperty("parts")
        private List<Part> parts;

        @JsonProperty("role")
        private String role;
    }

    @Data
    public static class Part {
        @JsonProperty("inlineData")
        private InlineData inlineData;
    }

    @Data
    public static class InlineData {
        @JsonProperty("mimeType")
        private String mimeType;

        @JsonProperty("data")
        private String data; // base64 encoded image
    }

    /**
     * 첫 번째 생성된 이미지의 base64 데이터 추출
     */
    public String getFirstImageBase64() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null) {
            return null;
        }

        List<Part> parts = candidate.getContent().getParts();
        if (parts.isEmpty()) {
            return null;
        }

        Part part = parts.get(0);
        if (part.getInlineData() == null) {
            return null;
        }

        return part.getInlineData().getData();
    }

    /**
     * 이미지 MIME 타입 추출
     */
    public String getFirstImageMimeType() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null) {
            return null;
        }

        List<Part> parts = candidate.getContent().getParts();
        if (parts.isEmpty()) {
            return null;
        }

        Part part = parts.get(0);
        if (part.getInlineData() == null) {
            return null;
        }

        return part.getInlineData().getMimeType();
    }
}
