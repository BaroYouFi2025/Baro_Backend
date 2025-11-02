package baro.baro.domain.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

// API Rate Limiter
// Google Gemini API의 RPM (분당 요청 수) 및 RPD (일일 요청 수) 제한 관리
@Component
@Slf4j
public class RateLimiter {

    private final Queue<Instant> requestTimestamps = new LinkedList<>();
    private final Queue<Instant> dailyRequestTimestamps = new LinkedList<>();

    private static final int DEFAULT_RPM = 10;  // 분당 10회
    private static final int DEFAULT_RPD = 100; // 일일 100회

    // 요청 가능 여부 확인 및 기록
    public synchronized boolean tryAcquire(int rpm, int rpd) {
        Instant now = Instant.now();

        // 분당 제한 체크
        if (rpm > 0) {
            cleanupOldRequests(requestTimestamps, Duration.ofMinutes(1), now);
            
            if (requestTimestamps.size() >= rpm) {
                Instant oldestRequest = requestTimestamps.peek();
                if (oldestRequest != null) {
                    long secondsUntilAvailable = Duration.between(now, oldestRequest.plus(Duration.ofMinutes(1))).getSeconds();
                    log.warn("분당 요청 제한 초과 ({}회/분) - {}초 후 재시도 가능", rpm, secondsUntilAvailable);
                    return false;
                }
            }
        }

        // 일일 제한 체크
        if (rpd > 0) {
            cleanupOldRequests(dailyRequestTimestamps, Duration.ofDays(1), now);
            
            if (dailyRequestTimestamps.size() >= rpd) {
                log.error("일일 요청 제한 초과 ({}회/일) - 내일까지 대기 필요", rpd);
                return false;
            }
        }

        // 요청 기록
        if (rpm > 0) {
            requestTimestamps.offer(now);
        }
        if (rpd > 0) {
            dailyRequestTimestamps.offer(now);
        }

        return true;
    }

    // 기본 제한값으로 요청 가능 여부 확인 (RPM=10, RPD=100)
    public boolean tryAcquire() {
        return tryAcquire(DEFAULT_RPM, DEFAULT_RPD);
    }

    // 오래된 요청 기록 정리
    private void cleanupOldRequests(Queue<Instant> queue, Duration window, Instant now) {
        while (!queue.isEmpty()) {
            Instant oldest = queue.peek();
            if (oldest != null && Duration.between(oldest, now).compareTo(window) > 0) {
                queue.poll();
            } else {
                break;
            }
        }
    }

    // 다음 요청 가능 시간까지 대기 시간(초) 계산
    public synchronized long getWaitTimeSeconds(int rpm) {
        if (rpm <= 0 || requestTimestamps.size() < rpm) {
            return 0;
        }

        Instant now = Instant.now();
        cleanupOldRequests(requestTimestamps, Duration.ofMinutes(1), now);

        if (requestTimestamps.size() < rpm) {
            return 0;
        }

        Instant oldestRequest = requestTimestamps.peek();
        if (oldestRequest == null) {
            return 0;
        }

        long waitTime = Duration.between(now, oldestRequest.plus(Duration.ofMinutes(1))).getSeconds();
        return Math.max(0, waitTime);
    }

    // 현재 분당 요청 수 조회
    public synchronized int getCurrentRpm() {
        cleanupOldRequests(requestTimestamps, Duration.ofMinutes(1), Instant.now());
        return requestTimestamps.size();
    }

    // 현재 일일 요청 수 조회
    public synchronized int getCurrentRpd() {
        cleanupOldRequests(dailyRequestTimestamps, Duration.ofDays(1), Instant.now());
        return dailyRequestTimestamps.size();
    }

    // 모든 기록 초기화 (테스트용)
    public synchronized void reset() {
        requestTimestamps.clear();
        dailyRequestTimestamps.clear();
        log.info("Rate limiter 초기화 완료");
    }
}
