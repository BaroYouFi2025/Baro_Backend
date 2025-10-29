package baro.baro.domain.ai.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Google Gemini Image Generation API 요청 DTO
 */
@Data
public class GeminiImageRequest {

    @JsonProperty("contents")
    private List<Content> contents;

    @JsonProperty("generationConfig")
    private GenerationConfig generationConfig;

    @Data
    public static class Content {
        @JsonProperty("parts")
        private List<Part> parts;
    }

    @Data
    public static class Part {
        @JsonProperty("text")
        private String text;
    }

    @Data
    public static class GenerationConfig {
        @JsonProperty("responseModalities")
        private List<String> responseModalities;

        @JsonProperty("imageConfig")
        private ImageConfig imageConfig;
    }

    @Data
    public static class ImageConfig {
        @JsonProperty("aspectRatio")
        private String aspectRatio;
    }

    /**
     * 1:1 비율 이미지 생성 요청 생성
     */
    public static GeminiImageRequest createSquare(String prompt) {
        GeminiImageRequest request = new GeminiImageRequest();

        Part part = new Part();
        part.setText(prompt);

        Content content = new Content();
        content.setParts(List.of(part));

        request.setContents(List.of(content));

        ImageConfig imageConfig = new ImageConfig();
        imageConfig.setAspectRatio("1:1");

        GenerationConfig generationConfig = new GenerationConfig();
        generationConfig.setResponseModalities(List.of("Image"));
        generationConfig.setImageConfig(imageConfig);

        request.setGenerationConfig(generationConfig);

        return request;
    }
}
