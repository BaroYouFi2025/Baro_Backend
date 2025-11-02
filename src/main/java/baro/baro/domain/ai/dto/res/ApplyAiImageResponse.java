package baro.baro.domain.ai.dto.res;

import baro.baro.domain.ai.entity.AssetType;
import lombok.Data;

// 선택한 AI 이미지를 적용한 결과 응답 DTO
@Data
public class ApplyAiImageResponse {
    private Long missingPersonId;
    private AssetType assetType;
    private String appliedUrl;

    // 응답 생성 팩토리 메서드
    public static ApplyAiImageResponse create(Long missingPersonId, AssetType assetType,
                                             String appliedUrl) {
        ApplyAiImageResponse response = new ApplyAiImageResponse();
        response.missingPersonId = missingPersonId;
        response.assetType = assetType;
        response.appliedUrl = appliedUrl;
        return response;
    }
}

