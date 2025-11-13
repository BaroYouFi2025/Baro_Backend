package baro.baro.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "전화번호 인증 요청")
@Getter
@NoArgsConstructor
public class PhoneVerifyRequest {
    @Schema(description = "전화번호 (11자리, 010으로 시작)", example = "01012345678")
    @NotBlank
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다.")
    private String phoneNumber;
}