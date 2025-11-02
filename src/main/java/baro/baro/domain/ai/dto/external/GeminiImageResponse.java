package baro.baro.domain.ai.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

// Google Gemini 이미지 생성/편집 API 응답 DTO
// Google Gemini 2.5 Flash Image 모델의 응답 형식을 따름
//
// 응답 예시:
// {
//   "candidates": [
//     {
//       "content": {
//         "parts": [
//           {
//             "inlineData": {
//               "mimeType": "image/png",
//               "data": "base64_encoded_image_data"
//             }
//           }
//         ]
//       }
//     }
//   ]
// }
//
// 참고: https://ai.google.dev/gemini-api/docs/vision
@Schema(description = "Google Gemini 이미지 생성/편집 응답")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiImageResponse {

    /**
     * 생성된 후보 결과 리스트
     */
    @Schema(description = "생성된 후보 결과 리스트")
    private List<Candidate> candidates;

    /**
     * 후보 결과
     */
    @Schema(description = "후보 결과")
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        /**
         * 콘텐츠 (이미지 또는 텍스트)
         */
        @Schema(description = "콘텐츠")
        private Content content;
    }

    /**
     * 콘텐츠
     */
    @Schema(description = "콘텐츠")
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        /**
         * 파트 리스트 (이미지 또는 텍스트)
         */
        @Schema(description = "파트 리스트")
        private List<Part> parts;
    }

    /**
     * 파트
     */
    @Schema(description = "파트")
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        /**
         * 인라인 이미지 데이터
         */
        @Schema(description = "인라인 이미지 데이터")
        private InlineData inlineData;  // camelCase로 변경

        /**
         * 텍스트 (에러 메시지 등)
         */
        @Schema(description = "텍스트")
        private String text;
    }

    /**
     * 인라인 이미지 데이터
     */
    @Schema(description = "인라인 이미지 데이터")
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InlineData {
        /**
         * MIME 타입
         */
        @Schema(description = "MIME 타입", example = "image/png")
        private String mimeType;  // camelCase로 변경

        /**
         * Base64 인코딩된 이미지 데이터
         */
        @Schema(description = "Base64 인코딩된 이미지 데이터")
        private String data;
    }

    // 첫 번째 이미지의 Base64 데이터 가져오기
    public String getFirstImageBase64() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Candidate firstCandidate = candidates.get(0);
        if (firstCandidate == null || firstCandidate.content == null || firstCandidate.content.parts == null) {
            return null;
        }

        for (Part part : firstCandidate.content.parts) {
            if (part != null && part.inlineData != null && part.inlineData.data != null) {
                return part.inlineData.data;
            }
        }

        return null;
    }

    // 응답에 텍스트 메시지가 있는지 확인 (에러 또는 정책 메시지)
    public String getTextMessage() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Candidate firstCandidate = candidates.get(0);
        if (firstCandidate == null || firstCandidate.content == null || firstCandidate.content.parts == null) {
            return null;
        }

        for (Part part : firstCandidate.content.parts) {
            if (part != null && part.text != null && !part.text.isEmpty()) {
                return part.text;
            }
        }

        return null;
    }

    // 이미지 데이터 존재 여부
    public boolean hasImage() {
        return getFirstImageBase64() != null;
    }
}
