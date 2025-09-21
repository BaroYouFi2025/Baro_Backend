package baro.baro.domain.auth.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokensResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn; // seconds
}


