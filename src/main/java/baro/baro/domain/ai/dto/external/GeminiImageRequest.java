package baro.baro.domain.ai.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

// Google Gemini 이미지 생성/편집 API 요청 DTO
// Google Gemini 2.5 Flash Image 모델을 사용하여 이미지를 생성하거나 편집
//
// API 엔드포인트:
// POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent
// Header: x-goog-api-key: YOUR_API_KEY
//
// 요청 예시 (이미지 + 텍스트):
// {
//   "contents": [
//     {
//       "parts": [
//         {
//           "inlineData": {
//             "mimeType": "image/jpeg",
//             "data": "base64_encoded_image_data"
//           }
//         },
//         {
//           "text": "Age this person to 30 years old"
//         }
//       ]
//     }
//   ],
//   "generationConfig": {
//     "responseModalities": ["Image"],
//     "imageConfig": {
//       "aspectRatio": "1:1"
//     }
//   }
// }
//
// 참고: https://ai.google.dev/gemini-api/docs/image-generation
@Schema(description = "Google Gemini 이미지 생성/편집 요청")
@Data
public class GeminiImageRequest {

    // 콘텐츠 리스트 (이미지 + 텍스트 프롬프트)
    @Schema(description = "콘텐츠 리스트", required = true)
    private List<Content> contents;

    // 생성 설정 (이미지 출력 모드, 종횡비 등)
    @Schema(description = "생성 설정")
    private GenerationConfig generationConfig;

    // 콘텐츠 (parts 배열 포함)
    @Schema(description = "콘텐츠")
    @Data
    public static class Content {
        // 파트 리스트 (이미지, 텍스트 등)
        @Schema(description = "파트 리스트 (이미지 inline_data와 텍스트 프롬프트)", required = true)
        private List<Part> parts;

        // Content 생성 (이미지 + 텍스트)
        public static Content create(String base64Image, String mimeType, String prompt) {
            Content content = new Content();
            content.parts = List.of(
                    Part.createImage(base64Image, mimeType),
                    Part.createText(prompt)
            );
            return content;
        }

        // Content 생성 (텍스트만)
        public static Content createTextOnly(String prompt) {
            Content content = new Content();
            content.parts = List.of(Part.createText(prompt));
            return content;
        }
    }

    // 파트 (이미지 또는 텍스트)
    @Schema(description = "파트 (이미지 또는 텍스트)")
    @Data
    public static class Part {
        // 인라인 이미지 데이터
        @Schema(description = "인라인 이미지 데이터")
        private InlineData inlineData;

        // 텍스트 프롬프트
        @Schema(description = "텍스트 프롬프트")
        private String text;

        // 이미지 파트 생성
        public static Part createImage(String base64Data, String mimeType) {
            Part part = new Part();
            part.inlineData = InlineData.create(mimeType, base64Data);
            return part;
        }

        // 텍스트 파트 생성
        public static Part createText(String text) {
            Part part = new Part();
            part.text = text;
            return part;
        }
    }

    // 인라인 이미지 데이터
    @Schema(description = "인라인 이미지 데이터")
    @Data
    public static class InlineData {
        // MIME 타입
        @Schema(description = "MIME 타입", example = "image/jpeg")
        private String mimeType;

        // Base64 인코딩된 이미지 데이터
        @Schema(description = "Base64 인코딩된 이미지 데이터")
        private String data;

        // InlineData 생성
        public static InlineData create(String mimeType, String base64Data) {
            InlineData inlineData = new InlineData();
            inlineData.mimeType = mimeType;
            inlineData.data = base64Data;
            return inlineData;
        }
    }

    // 생성 설정
    @Schema(description = "생성 설정")
    @Data
    public static class GenerationConfig {
        // 응답 모달리티 (이미지, 텍스트 등)
        @Schema(description = "응답 모달리티", example = "[\"Image\"]")
        private List<String> responseModalities;

        // 이미지 설정 (종횡비 등)
        @Schema(description = "이미지 설정")
        private ImageConfig imageConfig;

        // 기본 이미지 생성 설정
        public static GenerationConfig createImageMode() {
            GenerationConfig config = new GenerationConfig();
            config.responseModalities = List.of("Image");
            return config;
        }

        // 이미지 생성 설정 (종횡비 지정)
        public static GenerationConfig createImageMode(String aspectRatio) {
            GenerationConfig config = new GenerationConfig();
            config.responseModalities = List.of("Image");
            config.imageConfig = ImageConfig.create(aspectRatio);
            return config;
        }
    }

    // 이미지 설정
    @Schema(description = "이미지 설정")
    @Data
    public static class ImageConfig {
        // 종횡비
        @Schema(description = "종횡비", example = "1:1")
        private String aspectRatio;

        // ImageConfig 생성
        public static ImageConfig create(String aspectRatio) {
            ImageConfig config = new ImageConfig();
            config.aspectRatio = aspectRatio;
            return config;
        }
    }

    // 이미지 편집 요청 생성 (이미지 + 텍스트 프롬프트)
    public static GeminiImageRequest createImageEdit(String base64Image, String mimeType, String prompt) {
        GeminiImageRequest request = new GeminiImageRequest();
        request.contents = List.of(Content.create(base64Image, mimeType, prompt));
        request.generationConfig = GenerationConfig.createImageMode(); // responseModalities: ["Image"]
        return request;
    }

    // 텍스트 기반 이미지 생성 요청 (텍스트만)
    public static GeminiImageRequest createTextToImage(String prompt) {
        GeminiImageRequest request = new GeminiImageRequest();
        request.contents = List.of(Content.createTextOnly(prompt));
        request.generationConfig = GenerationConfig.createImageMode(); // responseModalities: ["Image"]
        return request;
    }
}