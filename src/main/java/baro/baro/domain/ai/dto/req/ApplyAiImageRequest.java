package baro.baro.domain.ai.dto.req;

import baro.baro.domain.common.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 선택한 AI 이미지를 MissingPerson에 적용하는 요청 DTO
 */
@Data
@Schema(description = "AI 이미지 적용 요청")
public class ApplyAiImageRequest {
    /**
     * 실종자 ID
     */
    @Schema(description = "실종자 ID", example = "1")
    private Long missingPersonId;

    /**
     * 에셋 타입 (AGE_PROGRESSION 또는 GENERATED_IMAGE)
     */
    @Schema(description = "이미지 타입", example = "AGE_PROGRESSION", allowableValues = {"GENERATED_IMAGE", "AGE_PROGRESSION"})
    private AssetType assetType;

    /**
     * 선택한 이미지 순서 (0, 1, 2 중 하나)
     */
    @Schema(description = "선택한 이미지 순서", example = "0", allowableValues = {"0", "1", "2"})
    private Integer sequenceOrder;

    /**
     * 요청 생성 팩토리 메서드
     *
     * @param missingPersonId 실종자 ID
     * @param assetType 에셋 타입
     * @param sequenceOrder 선택한 이미지 순서
     * @return 생성된 ApplyAiImageRequest 객체
     */
    public static ApplyAiImageRequest create(Long missingPersonId, AssetType assetType, Integer sequenceOrder) {
        ApplyAiImageRequest request = new ApplyAiImageRequest();
        request.missingPersonId = missingPersonId;
        request.assetType = assetType;
        request.sequenceOrder = sequenceOrder;
        return request;
    }
}

