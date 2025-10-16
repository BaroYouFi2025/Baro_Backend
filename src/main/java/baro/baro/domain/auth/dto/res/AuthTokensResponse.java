package baro.baro.domain.auth.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthTokensResponse {
    private String accessToken;
    private long expiresIn; // seconds
}


