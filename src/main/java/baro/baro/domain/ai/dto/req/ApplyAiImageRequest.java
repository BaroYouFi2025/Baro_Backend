package baro.baro.domain.ai.dto.req;

import baro.baro.domain.ai.entity.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 선택한 AI 이미지를 MissingPerson에 적용하는 요청 DTO
@Data
@Schema(description = "AI 이미지 적용 요청")
public class ApplyAiImageRequest {
    // 실종자 ID
    @Schema(description = "실종자 ID", example = "1")
    @NotNull(message = "실종자 ID는 필수입니다.")
    private Long missingPersonId;

    // 에셋 타입 (AGE_PROGRESSION 또는 GENERATED_IMAGE)
    @Schema(description = "이미지 타입", example = "AGE_PROGRESSION", allowableValues = {"GENERATED_IMAGE", "AGE_PROGRESSION"})
    @NotNull(message = "에셋 타입은 필수입니다.")
    private AssetType assetType;

    // 선택한 이미지 URL
    @Schema(description = "선택한 이미지 url", example = "http://example.com/image.jpg")
    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String selectedImageUrl;

    // 요청 생성 팩토리 메서드
    public static ApplyAiImageRequest create(Long missingPersonId, AssetType assetType, String selectedImageUrl) {
        ApplyAiImageRequest request = new ApplyAiImageRequest();
        request.missingPersonId = missingPersonId;
        request.assetType = assetType;
        request.selectedImageUrl= selectedImageUrl;
        return request;
    }
}

