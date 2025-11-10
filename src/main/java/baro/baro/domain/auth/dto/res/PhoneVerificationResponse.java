package baro.baro.domain.auth.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "전화번호 인증 토큰 응답")
@Data
@AllArgsConstructor
public class PhoneVerificationResponse {
    @Schema(description = "생성된 인증 토큰", example = "abc123def456")
    private String token;
}