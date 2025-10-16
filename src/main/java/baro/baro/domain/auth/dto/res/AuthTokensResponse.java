package baro.baro.domain.auth.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "JWT 토큰 응답")
@Getter
@AllArgsConstructor
public class AuthTokensResponse {
    @Schema(description = "액세스 토큰 (API 요청 시 사용)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "리프레시 토큰 (액세스 토큰 갱신 시 사용)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
    private long expiresIn;
}


