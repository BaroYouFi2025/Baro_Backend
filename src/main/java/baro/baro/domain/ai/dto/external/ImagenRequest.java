package baro.baro.domain.ai.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Google Imagen API 요청 DTO
 *
 * <p>Google Imagen 3.0 API의 REST 요청 형식을 따릅니다.</p>
 *
 * <p><b>API 엔드포인트:</b></p>
 * <pre>
 * POST https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict
 * Header: x-goog-api-key: YOUR_API_KEY
 * </pre>
 *
 * <p><b>요청 예시:</b></p>
 * <pre>
 * {
 *   "instances": [
 *     { "prompt": "A photo of a cat" }
 *   ],
 *   "parameters": {
 *     "sampleCount": 1,
 *     "aspectRatio": "1:1",
 *     "safetyFilterLevel": "block_some",
 *     "personGeneration": "allow_adult"
 *   }
 * }
 * </pre>
 *
 * @see <a href="https://ai.google.dev/gemini-api/docs/imagen">Imagen API 문서</a>
 */
@Schema(description = "Google Imagen API 이미지 생성 요청")
@Data
public class ImagenRequest {

    /**
     * 이미지 생성 프롬프트 인스턴스 리스트
     */
    @Schema(description = "이미지 생성 프롬프트 인스턴스 리스트", required = true)
    private List<Instance> instances;

    /**
     * 이미지 생성 파라미터
     */
    @Schema(description = "이미지 생성 파라미터")
    private Parameters parameters;

    /**
     * 프롬프트 인스턴스
     */
    @Schema(description = "프롬프트 인스턴스")
    @Data
    public static class Instance {
        /**
         * 이미지 생성 텍스트 프롬프트
         */
        @Schema(description = "이미지 생성 텍스트 프롬프트", example = "A realistic portrait of a person", required = true)
        private String prompt;

        public static Instance create(String prompt) {
            Instance instance = new Instance();
            instance.prompt = prompt;
            return instance;
        }
    }

    /**
     * 이미지 생성 파라미터
     */
    @Schema(description = "이미지 생성 파라미터")
    @Data
    public static class Parameters {
        /**
         * 생성할 이미지 수 (1-4)
         */
        @Schema(description = "생성할 이미지 수", example = "1", minimum = "1", maximum = "4")
        private Integer sampleCount;

        /**
         * 이미지 비율
         * - "1:1" (정사각형, 1024x1024)
         * - "3:4" (세로, 768x1024)
         * - "4:3" (가로, 1024x768)
         * - "9:16" (세로, 576x1024)
         * - "16:9" (가로, 1024x576)
         */
        @Schema(description = "이미지 비율", example = "1:1", allowableValues = {"1:1", "3:4", "4:3", "9:16", "16:9"})
        private String aspectRatio;

        /**
         * 안전 필터 레벨
         * - "block_most" (대부분 차단)
         * - "block_some" (일부 차단, 기본값)
         * - "block_few" (최소 차단)
         */
        @Schema(description = "안전 필터 레벨", example = "block_some", allowableValues = {"block_most", "block_some", "block_few"})
        private String safetyFilterLevel;

        /**
         * 사람 생성 허용 여부
         * - "dont_allow" (불허)
         * - "allow_adult" (성인만 허용, 기본값)
         */
        @Schema(description = "사람 생성 허용 여부", example = "allow_adult", allowableValues = {"dont_allow", "allow_adult"})
        private String personGeneration;

        public static Parameters createDefault() {
            Parameters params = new Parameters();
            params.sampleCount = 1;
            params.aspectRatio = "1:1";
            params.safetyFilterLevel = "block_some";
            params.personGeneration = "allow_adult";
            return params;
        }

        public static Parameters createSquare(int sampleCount) {
            Parameters params = createDefault();
            params.sampleCount = sampleCount;
            return params;
        }
    }

    /**
     * 기본 요청 생성 (1장, 정사각형)
     */
    public static ImagenRequest create(String prompt) {
        ImagenRequest request = new ImagenRequest();
        request.instances = List.of(Instance.create(prompt));
        request.parameters = Parameters.createDefault();
        return request;
    }

    /**
     * 여러 장 생성 요청
     */
    public static ImagenRequest create(String prompt, int sampleCount) {
        ImagenRequest request = new ImagenRequest();
        request.instances = List.of(Instance.create(prompt));
        request.parameters = Parameters.createSquare(sampleCount);
        return request;
    }
}
