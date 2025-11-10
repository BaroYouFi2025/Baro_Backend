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
}
