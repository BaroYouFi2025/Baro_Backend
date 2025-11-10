package baro.baro.domain.ai.dto.req;

import baro.baro.domain.ai.entity.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// AI 이미지 생성 요청 DTO
@Data
@Schema(description = "AI 이미지 생성 요청")
public class GenerateAiImageRequest {

    // 실종자 ID
    @Schema(description = "실종자 ID", example = "1")
    @NotNull(message = "실종자 ID는 필수입니다.")
    private Long missingPersonId;

    // 에셋 타입 (AGE_PROGRESSION 또는 GENERATED_IMAGE)
    @Schema(description = "생성할 이미지 타입", example = "AGE_PROGRESSION", allowableValues = {"GENERATED_IMAGE", "AGE_PROGRESSION"})
    @NotNull(message = "에셋 타입은 필수입니다.")
    private AssetType assetType;

    // 요청 생성 팩토리 메서드
    public static GenerateAiImageRequest create(Long missingPersonId, AssetType assetType) {
        GenerateAiImageRequest request = new GenerateAiImageRequest();
        request.missingPersonId = missingPersonId;
        request.assetType = assetType;
        return request;
    }
}
