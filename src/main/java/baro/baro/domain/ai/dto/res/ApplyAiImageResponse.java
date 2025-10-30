package baro.baro.domain.ai.dto.res;

import baro.baro.domain.common.enums.AssetType;
import lombok.Data;

/**
 * 선택한 AI 이미지를 적용한 결과 응답 DTO
 */
@Data
public class ApplyAiImageResponse {
    private Long missingPersonId;
    private AssetType assetType;
    private String appliedUrl;

    /**
     * 응답 생성 팩토리 메서드
     *
     * @param missingPersonId 실종자 ID
     * @param assetType 에셋 타입
     * @param appliedUrl 적용된 이미지 URL
     * @return 생성된 ApplyAiImageResponse 객체
     */
    public static ApplyAiImageResponse create(Long missingPersonId, AssetType assetType,
                                             String appliedUrl) {
        ApplyAiImageResponse response = new ApplyAiImageResponse();
        response.missingPersonId = missingPersonId;
        response.assetType = assetType;
        response.appliedUrl = appliedUrl;
        return response;
    }
}

