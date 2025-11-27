package baro.baro.domain.auth.entity;

import jakarta.persistence.*;
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
@Table(name = "phone_verification", schema = "youfi")
public class PhoneVerification {
    @Id @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name ="phone_number")
    private String phoneNumber;
    @Column(name = "verified", nullable = false)

    private boolean verified;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public void verifyPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
            this.verified = true;
        }
    }

    // 인증 완료 여부를 반환합니다.
    //
    // @return 인증 완료된 경우 true, 그렇지 않으면 false
    public boolean isVerified() {
        return this.verified;
    }
}
