package baro.baro.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 블랙리스트에 등록된 Refresh Token 엔티티
// 로그아웃되거나 무효화된 토큰을 추적합니다.
@Entity
@Table(name = "blacklisted_tokens", schema = "youfi",indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 블랙리스트에 등록된 Refresh Token
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    // 토큰의 만료 시간
    // 이 시간 이후에는 자동으로 삭제 가능
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 블랙리스트 등록 시간
    @Column(nullable = false)
    private LocalDateTime blacklistedAt;

    // 블랙리스트 등록 사유 (예: LOGOUT, TOKEN_REFRESH, SECURITY_BREACH)
    @Column(length = 50)
    private String reason;

    // 토큰 소유자 UID
    @Column(length = 100)
    private String userId;

    // 블랙리스트 토큰 생성자
    public BlacklistedToken(String token, LocalDateTime expiresAt, String reason, String userId) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.blacklistedAt = LocalDateTime.now();
        this.reason = reason;
        this.userId = userId;
    }

    // 토큰이 만료되었는지 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
