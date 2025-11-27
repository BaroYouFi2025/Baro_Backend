package baro.baro.domain.device.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "FCM 토큰 업데이트 요청")
public class FcmTokenUpdateRequest {

    @NotBlank(message = "FCM token is required")
    @Schema(description = "FCM 토큰", example = "fcm-token-string")
    private String fcmToken;
}
