package baro.baro.domain.policeoffice.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Schema(description = "주변 경찰관서 검색 요청")
public class NearbyPoliceOfficeRequest {

    public static final int DEFAULT_RADIUS_METERS = 5000;
    public static final int DEFAULT_LIMIT = 5;
    public static final int MAX_LIMIT = 30;

    @Schema(description = "위도", example = "37.5665")
    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    @Schema(description = "검색 반경 (미터)", example = "5000", defaultValue = "5000")
    @Min(value = 1, message = "반경은 1m 이상이어야 합니다.")
    private Integer radiusMeters = DEFAULT_RADIUS_METERS;

    @Schema(description = "최대 결과 수", example = "5", defaultValue = "5")
    @Min(value = 1, message = "최소 1개 이상 조회해야 합니다.")
    private Integer limit = DEFAULT_LIMIT;

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setRadiusMeters(Integer radiusMeters) {
        this.radiusMeters = (radiusMeters == null || radiusMeters < 1)
                ? DEFAULT_RADIUS_METERS
                : radiusMeters;
    }

    public void setLimit(Integer limit) {
        if (limit == null || limit < 1) {
            this.limit = DEFAULT_LIMIT;
        } else {
            this.limit = Math.min(limit, MAX_LIMIT);
        }
    }
}
