package baro.baro.domain.device.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "기기 등록 요청")
public class DeviceRegisterRequest {

    @NotNull(message = "Device UUID is required")
    @Schema(description = "기기 고유 식별자 (클라이언트가 생성)", example = "device-uuid-1234")
    private String deviceUuid;

    @Schema(description = "OS 타입", example = "iOS")
    private String osType;

    @Schema(description = "OS 버전", example = "17.0")
    private String osVersion;

    @Schema(description = "FCM 토큰 (푸시 알림용)", example = "fcm-token-string")
    private String fcmToken;
}
