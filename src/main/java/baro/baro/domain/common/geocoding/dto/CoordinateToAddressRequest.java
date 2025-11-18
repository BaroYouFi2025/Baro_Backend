package baro.baro.domain.common.geocoding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "좌표를 주소로 변환 요청")
@Getter
@Setter
@NoArgsConstructor
public class CoordinateToAddressRequest {

    @Schema(description = "위도", example = "37.5665")
    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;
}
