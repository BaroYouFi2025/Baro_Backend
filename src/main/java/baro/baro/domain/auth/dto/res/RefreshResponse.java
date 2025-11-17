package baro.baro.domain.auth.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "토큰 갱신 응답")
public class RefreshResponse {
    @Schema(description = "새로운 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "새로운 리프레시 토큰", example = "eyJhbGci0OiJIUzI1NiIs...")
    private String refreshToken;

    @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
    private long expiresIn;
}