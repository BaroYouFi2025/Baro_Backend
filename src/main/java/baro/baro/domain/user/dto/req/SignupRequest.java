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

    @Schema(description = "사용자 ID (4~20자)", example = "newUs")
    @NotBlank
    @Size(min = 4, max = 20)
    private String uid;

    @Schema(description = "비밀번호 (8~20자)", example = "StrongPass123!")
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @Schema(description = "전화번호 (11자리, 하이픈 없음)", example = "010-1234-5678")
    @NotBlank
    @Size(min = 11, max = 11)
    private String phone;

    @Schema(description = "사용자 이름 (1~20자)", example = "김이수")
    @NotBlank
    @Size(min = 1, max = 20)
    private String username;

    @Schema(description = "생년월일 (yyyy-MM-dd 형식)", example = "2000-09-09")
    @NotBlank
    private String birthDate;
}


