package baro.baro.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 위치 정보 DTO
 *
 * GPS 좌표(위도, 경도)를 표현합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "위치 정보")
public class LocationInfo {

    @Schema(description = "위도", example = "35.1763")
    private Double latitude;

    @Schema(description = "경도", example = "128.9664")
    private Double longitude;
}
