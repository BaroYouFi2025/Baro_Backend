package baro.baro.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PhoneVerifyRequest {
    @NotBlank
    @Size(min = 11, max = 11)
    @Pattern(regexp = "^010\\d{8}$")
    private String phoneNumber;
}