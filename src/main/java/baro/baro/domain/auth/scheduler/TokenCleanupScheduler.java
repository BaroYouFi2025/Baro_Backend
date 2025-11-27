package baro.baro.domain.auth.scheduler;

import baro.baro.domain.auth.repository.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 만료된 블랙리스트 토큰을 정기적으로 정리하는 스케줄러
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // 매일 새벽 3시에 만료된 블랙리스트 토큰을 삭제합니다.
    // DB 공간 확보 및 성능 유지를 위해 필요합니다.
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("만료된 블랙리스트 토큰 정리 시작");
        
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(now);
        
        log.info("만료된 블랙리스트 토큰 정리 완료 - 삭제된 토큰 수: {}", deletedCount);
    }
}
