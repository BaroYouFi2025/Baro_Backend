package baro.baro.domain.ai.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Google Imagen API 응답 DTO
 *
 * <p>Google Imagen 3.0 API의 REST 응답 형식을 따릅니다.</p>
 *
 * <p><b>응답 예시:</b></p>
 * <pre>
 * {
 *   "predictions": [
 *     {
 *       "bytesBase64Encoded": "iVBORw0KGgoAAAANSUhEUgAA...",
 *       "mimeType": "image/png"
 *     }
 *   ]
 * }
 * </pre>
 *
 * @see <a href="https://ai.google.dev/gemini-api/docs/imagen">Imagen API 문서</a>
 */
@Schema(description = "Google Imagen API 이미지 생성 응답")
@Data
public class ImagenResponse {

    /**
     * 생성된 이미지 예측 결과 리스트
     */
    @Schema(description = "생성된 이미지 예측 결과 리스트")
    private List<Prediction> predictions;

    /**
     * 이미지 예측 결과
     */
    @Schema(description = "이미지 예측 결과")
    @Data
    public static class Prediction {
        /**
         * Base64 인코딩된 이미지 데이터
         */
        @Schema(description = "Base64 인코딩된 이미지 데이터", example = "iVBORw0KGgoAAAANSUhEUgAA...")
        private String bytesBase64Encoded;

        /**
         * MIME 타입 (예: "image/png")
         */
        @Schema(description = "MIME 타입", example = "image/png")
        private String mimeType;
    }

    /**
     * 첫 번째 이미지의 Base64 데이터 가져오기
     *
     * @return Base64 인코딩된 이미지 문자열, 없으면 null
     */
    public String getFirstImageBase64() {
        if (predictions == null || predictions.isEmpty()) {
            return null;
        }
        Prediction first = predictions.get(0);
        return first != null ? first.getBytesBase64Encoded() : null;
    }

    /**
     * 모든 이미지의 Base64 데이터 가져오기
     *
     * @return Base64 인코딩된 이미지 문자열 리스트
     */
    public List<String> getAllImagesBase64() {
        if (predictions == null) {
            return List.of();
        }
        return predictions.stream()
                .map(Prediction::getBytesBase64Encoded)
                .filter(bytes -> bytes != null && !bytes.isEmpty())
                .toList();
    }

    /**
     * 생성된 이미지 개수
     */
    public int getImageCount() {
        return predictions != null ? predictions.size() : 0;
    }
}
