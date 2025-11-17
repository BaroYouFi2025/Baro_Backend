package baro.baro.domain.missingperson.dto.req;

import baro.baro.domain.missingperson.entity.GenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실종자 등록 요청")
public class RegisterMissingPersonRequest {

    @Schema(description = "실종자명", example = "김실종")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "생년월일", example = "2015-09-12")
    @NotBlank(message = "생년월일은 필수입니다.")
    private String birthDate;

    @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE", "UNKNOWN"})
    private String gender;

    @Schema(description = "실종자 사진 URL", example = "https://example.com/photo.jpg")
    private String photoUrl;

    @Schema(description = "실종 일시", example = "2025-10-01T14:30:00")
    @NotBlank(message = "실종일은 필수입니다.")
    private String missingDate;

    @Schema(description = "키 (cm)", example = "165")
    @Min(value = 0, message = "키는 0 이상이어야 합니다.")
    private Integer height;

    @Schema(description = "몸무게 (kg)", example = "55")
    @Min(value = 0, message = "몸무게는 0 이상이어야 합니다.")
    private Integer weight;

    @Schema(description = "체형", example = "보통")
    private String body;

    @Schema(description = "기타 신체", example = "왼쪽 귀에 점이 2개 있음")
    private String bodyEtc;

    @Schema(description = "상의", example = "티니핑 티")
    private String clothesTop;

    @Schema(description = "하의", example = "노란 바지")
    private String clothesBottom;

    @Schema(description = "기타 의류", example = "회색 바람막이")
    private String clothesEtc;

    @Schema(description = "위도", example = "35.188884")
    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @Schema(description = "경도", example = "128.903480")
    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;
    
    public static RegisterMissingPersonRequest create(
            String name, String birthDate, String photoUrl, String missingDate,
            Integer height, Integer weight, String body, String bodyEtc,
            String clothesTop, String clothesBottom, String clothesEtc,
            Double latitude, Double longitude, String gender) {
        RegisterMissingPersonRequest request = new RegisterMissingPersonRequest();
        request.name = name;
        request.birthDate = birthDate;
        request.photoUrl = photoUrl;
        request.missingDate = missingDate;
        request.gender = gender;
        request.height = height;
        request.weight = weight;
        request.body = body;
        request.bodyEtc = bodyEtc;
        request.clothesTop = clothesTop;
        request.clothesBottom = clothesBottom;
        request.clothesEtc = clothesEtc;
        request.latitude = latitude;
        request.longitude = longitude;
        return request;
    }
}
