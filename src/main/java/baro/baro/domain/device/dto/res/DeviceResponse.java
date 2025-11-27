package baro.baro.domain.device.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기기 정보 응답")
public class DeviceResponse {

    @Schema(description = "기기 ID", example = "123")
    private Long deviceId;

    @Schema(description = "기기 UUID", example = "device-uuid-1234")
    private String deviceUuid;

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

    @Schema(description = "FCM 토큰", example = "fcm-token-string")
    private String fcmToken;
}
