package baro.baro.domain.auth.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PhoneVerificationResponse {
    private String token;
}