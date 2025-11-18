package baro.baro.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "테스트용 전화번호 인증 요청")
@Getter
@Setter
@NoArgsConstructor
public class TestPhoneVerificationRequest {

    @Schema(description = "인증 토큰", example = "abc123token")
    @NotBlank(message = "토큰은 필수입니다.")
    private String token;

    @Schema(description = "인증할 전화번호 (11자리)", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호는 11자리 숫자여야 합니다.")
    private String phoneNumber;
}
