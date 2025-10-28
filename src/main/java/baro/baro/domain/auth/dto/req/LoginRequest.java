package baro.baro.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "User ID is required")
    private String uid;
    
    @NotBlank(message = "Password is required")
    private String password;
}
