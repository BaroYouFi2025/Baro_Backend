package baro.baro.domain.device.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기기 정보 응답")
public class DeviceResponse {

    @Schema(description = "기기 ID", example = "123")
    private Long deviceId;

    @Schema(description = "기기 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID deviceUuid;

    @Schema(description = "배터리 잔량", example = "85")
    private Integer batteryLevel;

    @Schema(description = "OS 타입", example = "iOS")
    private String osType;

    @Schema(description = "OS 버전", example = "17.0")
    private String osVersion;

    @Schema(description = "활성화 상태", example = "true")
    private boolean isActive;

    @Schema(description = "등록 시간", example = "2025-10-20T12:00:00")
    private LocalDateTime registeredAt;
}
