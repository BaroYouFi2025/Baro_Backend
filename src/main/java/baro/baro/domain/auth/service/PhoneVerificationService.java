package baro.baro.domain.auth.service;

import baro.baro.domain.auth.entity.PhoneVerification;
import baro.baro.domain.auth.exception.PhoneVerificationErrorCode;
import baro.baro.domain.auth.exception.PhoneVerificationException;
import baro.baro.domain.auth.repository.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final PhoneVerificationRepository repo;

    // 토큰 유효시간 (10분)
    private static final long VALIDITY_SECONDS = 600;

    /**
     * 인증 토큰 생성 및 저장
     */
    @Transactional
    public String createVerificationToken() {
        String token;
        int retryCount = 0;

        // 중복 토큰 방지를 위한 재시도 로직
        do {
            token = generateRandomCode(6);
            retryCount++;

            if (retryCount > 5) {
                throw new PhoneVerificationException(PhoneVerificationErrorCode.TOKEN_GENERATION_FAILED);
            }
        } while (repo.findByTokenAndVerifiedFalse(token).isPresent());

        PhoneVerification pv = PhoneVerification.builder()
                .token(token)
                .verified(false)
                .expiresAt(LocalDateTime.now().plusSeconds(VALIDITY_SECONDS))
                .build();

        repo.save(pv);
        return token;
    }

    /**
     * 토큰을 사용한 전화번호 인증 처리
     */
    @Transactional
    protected void authenticateWithToken(String token, String phoneNumber) {
        if (token == null || token.trim().isEmpty()) {
            throw new PhoneVerificationException(PhoneVerificationErrorCode.INVALID_TOKEN);
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new PhoneVerificationException(PhoneVerificationErrorCode.PHONE_NUMBER_REQUIRED);
        }

        PhoneVerification pv = repo.findByTokenAndVerifiedFalse(token)
                .orElseThrow(() -> new PhoneVerificationException(PhoneVerificationErrorCode.INVALID_TOKEN));

        if (pv.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PhoneVerificationException(PhoneVerificationErrorCode.TOKEN_EXPIRED);
        }

        // 이미 다른 번호로 인증된 토큰인지 확인
        if (pv.getPhoneNumber() != null && !pv.getPhoneNumber().equals(phoneNumber)) {
            throw new PhoneVerificationException(PhoneVerificationErrorCode.TOKEN_MISMATCH);
        }

        try {
            pv.verifyPhoneNumber(phoneNumber);
            log.info("토큰 인증 성공: token={}, phoneNumber={}", token, phoneNumber);
        } catch (Exception e) {
            log.error("토큰 인증 중 예상치 못한 오류: token={}, phoneNumber={}, error={}",
                     token, phoneNumber, e.getMessage());
            throw new PhoneVerificationException(PhoneVerificationErrorCode.VERIFICATION_FAILED);
        }
    }

    /**
     *  전화번호 인증 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isPhoneNumberVerified(String phoneNumber) {
        PhoneVerification phoneVerification = repo.findByPhoneNumber(phoneNumber).orElse(null);
        return phoneVerification != null && phoneVerification.isVerified();
    }

    /**
     * 만료된 토큰들을 주기적으로 삭제
     * 매일 새벽 2시에 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = repo.deleteExpiredTokens(now);
        log.info("만료된 토큰 {}개 삭제 완료", deletedCount);
    }

    /**
     * 랜덤 숫자 코드 생성
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}