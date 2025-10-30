package baro.baro.domain.ai.dto.res;

import baro.baro.domain.common.enums.AssetType;
import lombok.Data;

import java.util.List;

/**
 * AI 이미지 생성 응답 DTO
 *
 * <p>AI 이미지 생성 요청에 대한 응답 데이터를 담습니다.
 * 생성된 이미지들의 URL과 에셋 타입 정보를 포함합니다.</p>
 *
 * <p><b>응답 예시:</b></p>
 * <pre>
 * {
 *   "assetType": "AGE_PROGRESSION",
 *   "imageUrls": [
 *     "https://storage.googleapis.com/baro-ai-assets/image1.jpg",
 *     "https://storage.googleapis.com/baro-ai-assets/image2.jpg",
 *     "https://storage.googleapis.com/baro-ai-assets/image3.jpg"
 *   ]
 * }
 * </pre>
 */
@Data
public class GenerateAiImageResponse {

    /**
     * 에셋 타입
     * 생성된 이미지의 타입 (AGE_PROGRESSION 또는 DESCRIPTION)
     */
    private AssetType assetType;

    /**
     * 생성된 이미지 URL 리스트
     * AGE_PROGRESSION의 경우 3개
     * DESCRIPTION의 경우 1개 (인상착의 기반 이미지)
     */
    private List<String> imageUrls;

    /**
     * 응답 생성 팩토리 메서드
     *
     * <p>에셋 타입과 이미지 URL 리스트로 응답 객체를 생성합니다.</p>
     *
     * @param assetType 생성된 이미지의 타입
     * @param imageUrls 생성된 이미지 URL 리스트
     * @return 생성된 GenerateAiImageResponse 객체
     */
    public static GenerateAiImageResponse create(AssetType assetType, List<String> imageUrls) {
        GenerateAiImageResponse response = new GenerateAiImageResponse();
        response.assetType = assetType;
        response.imageUrls = imageUrls;
        return response;
    }
}
