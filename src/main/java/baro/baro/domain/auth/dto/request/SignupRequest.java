package baro.baro.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @Size(min = 4, max = 20)
    private String uid;

    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

    @NotBlank
    private String phone;

    @NotBlank
    @Size(min = 1, max = 20)
    private String username;

    @NotBlank
    // yyyy-MM-dd
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String birthDate;
}


