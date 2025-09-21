package baro.baro.domain.auth.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LogoutRequest {
    private String refreshToken;
}
