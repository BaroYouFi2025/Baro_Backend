package baro.baro.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 요청")
@Getter
@NoArgsConstructor
public class SignupRequest {

    @Schema(description = "사용자 ID (4~20자)", example = "user1234", required = true)
    @NotBlank
    @Size(min = 4, max = 20)
    private String uid;

    @Schema(description = "비밀번호 (8~20자)", example = "password123!", required = true)
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @Schema(description = "전화번호 (11자리, 하이픈 없음)", example = "01012345678", required = true)
    @NotBlank
    @Size(min = 11, max = 11)
    private String phone;

    @Schema(description = "사용자 이름 (1~20자)", example = "홍길동", required = true)
    @NotBlank
    @Size(min = 1, max = 20)
    private String username;

    @Schema(description = "생년월일 (yyyy-MM-dd 형식)", example = "1990-01-01", required = true)
    @NotBlank
    private String birthDate;
}


