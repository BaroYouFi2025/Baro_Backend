package baro.baro.domain.auth.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "전화번호 인증 상태 응답")
@Data
@AllArgsConstructor
public class PhoneVerifyResponse {
    @Schema(description = "인증 완료 여부", example = "true")
    private boolean verified;
}