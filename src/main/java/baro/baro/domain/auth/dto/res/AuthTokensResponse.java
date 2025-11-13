package baro.baro.domain.auth.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "JWT 토큰 응답")
@Getter
public class AuthTokensResponse {
    @Schema(description = "액세스 토큰 (API 요청 시 사용)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGci0OiJIUzI1NiIs...")
    private String refreshToken;

    @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
    private long expiresIn;
    
    // 회원가입용 생성자 (refreshToken 포함)
    public AuthTokensResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
    
    // 로그인용 생성자 (refreshToken 없음, 쿠키로만 전달)
    public static AuthTokensResponse forLogin(String accessToken, long expiresIn) {
        return new AuthTokensResponse(accessToken, null, expiresIn);
    }
}


