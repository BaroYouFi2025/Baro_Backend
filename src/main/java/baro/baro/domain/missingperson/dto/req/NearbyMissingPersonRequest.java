package baro.baro.domain.missingperson.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyMissingPersonRequest {
    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

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
