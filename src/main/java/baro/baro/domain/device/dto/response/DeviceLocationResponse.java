package baro.baro.domain.device.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "기기 위치 조회 응답")
public class DeviceLocationResponse {

    @Schema(description = "위도", example = "35.1763")
    private Double latitude;

    @Schema(description = "경도", example = "128.9664")
    private Double longitude;

    @Schema(description = "배터리 잔량 (0-100)", example = "85", nullable = true)
    private Integer batteryLevel;

    @Schema(description = "위치 기록 시간", example = "2025-10-21T19:30:00")
    private LocalDateTime recordedAt;
}
