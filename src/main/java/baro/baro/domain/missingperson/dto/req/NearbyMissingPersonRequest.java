package baro.baro.domain.missingperson.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Schema(description = "주변 실종자 검색 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyMissingPersonRequest {
    @Schema(description = "위도", example = "35.1763")
    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @Schema(description = "경도", example = "128.9664")
    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    @Schema(description = "반경 (미터)", example = "1000")
    @NotNull(message = "반경은 필수입니다.")
    @Min(value = 1, message = "반경은 1 이상이어야 합니다.")
    private Integer radius;

    public static NearbyMissingPersonRequest create(Double latitude, Double longitude, Integer radius) {
        NearbyMissingPersonRequest request = new NearbyMissingPersonRequest();
        request.latitude = latitude;
        request.longitude = longitude;
        request.radius = radius;
        return request;
    }
}
