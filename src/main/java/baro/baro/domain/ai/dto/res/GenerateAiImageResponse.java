package baro.baro.domain.ai.dto.res;

import baro.baro.domain.ai.entity.AssetType;
import lombok.Data;

import java.util.List;

// AI 이미지 생성 응답 DTO
@Data
public class GenerateAiImageResponse {

    // 생성된 이미지의 타입 (AGE_PROGRESSION 또는 GENERATED_IMAGE)
    private AssetType assetType;

    // 생성된 이미지 URL 리스트
    private List<String> imageUrls;

    // 응답 생성 팩토리 메서드
    public static GenerateAiImageResponse create(AssetType assetType, List<String> imageUrls) {
        GenerateAiImageResponse response = new GenerateAiImageResponse();
        response.assetType = assetType;
        response.imageUrls = imageUrls;
        return response;
    }
}
