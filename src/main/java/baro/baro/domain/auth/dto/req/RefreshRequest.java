package baro.baro.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
