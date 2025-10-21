package baro.baro.domain.device.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "기기 등록 요청")
public class DeviceRegisterRequest {

    @NotNull(message = "Device UUID is required")
    @Schema(description = "기기 고유 식별자 (클라이언트가 생성)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID deviceUuid;

    @Schema(description = "OS 타입", example = "iOS")
    private String osType;

    @Schema(description = "OS 버전", example = "17.0")
    private String osVersion;
}
