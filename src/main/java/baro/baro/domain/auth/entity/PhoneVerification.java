package baro.baro.domain.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneVerification {
    @Id @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private String phoneNumber;
    private boolean verified;
    private LocalDateTime expiresAt;

    public void verifyPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
            this.verified = true;
        }
    }
}
