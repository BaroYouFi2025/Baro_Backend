package baro.baro.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "로그인 요청")
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    @Schema(description = "사용자 ID", example = "baro")
    @NotBlank(message = "User ID is required")
    private String uid;

    @Schema(description = "비밀번호", example = "barobaro")
    @NotBlank(message = "Password is required")
    private String password;

    @Schema(description = "기기 UUID (선택사항, 제공 시 해당 기기 활성화)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String deviceUuid;
}
