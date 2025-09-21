package baro.baro.domain.auth.repository;

import baro.baro.domain.auth.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    // 특정 토큰에 대해 아직 인증 안 된 기록 찾기
    Optional<PhoneVerification> findByTokenAndVerifiedFalse(String token);

    // 특정 전화번호에 대해 인증된 기록 찾기
    Optional<PhoneVerification> findByPhoneNumber(String phoneNumber);

    // 만료된 토큰들 삭제
    @Modifying
    @Query("DELETE FROM PhoneVerification p WHERE p.expiresAt < :now")
    int deleteExpiredTokens(LocalDateTime now);
}
