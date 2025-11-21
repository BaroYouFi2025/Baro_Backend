package baro.baro.domain.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }

    @Test
    void tryAcquireRespectsPerMinuteLimit() {
        assertThat(rateLimiter.tryAcquire(2, 10)).isTrue();
        assertThat(rateLimiter.tryAcquire(2, 10)).isTrue();

        assertThat(rateLimiter.tryAcquire(2, 10)).isFalse();
        assertThat(rateLimiter.getCurrentRpm()).isEqualTo(2);
        assertThat(rateLimiter.getWaitTimeSeconds(2)).isGreaterThan(0L);
    }

    @Test
    void defaultTryAcquireUsesPresetLimits() {
        for (int i = 0; i < 10; i++) {
            assertThat(rateLimiter.tryAcquire()).isTrue();
        }

        assertThat(rateLimiter.tryAcquire()).isFalse();
        assertThat(rateLimiter.getCurrentRpm()).isEqualTo(10);
    }

    @Test
    void tryAcquireRespectsDailyLimit() {
        assertThat(rateLimiter.tryAcquire(10, 2)).isTrue();
        assertThat(rateLimiter.tryAcquire(10, 2)).isTrue();

        assertThat(rateLimiter.tryAcquire(10, 2)).isFalse();
        assertThat(rateLimiter.getCurrentRpd()).isEqualTo(2);
    }

    @Test
    void getWaitTimeSecondsRemovesExpiredEntries() {
        Queue<Instant> requestQueue = getRequestQueue();
        requestQueue.clear();

        Instant now = Instant.now();
        requestQueue.offer(now.minusSeconds(90));
        requestQueue.offer(now.minusSeconds(5));

        long waitTime = rateLimiter.getWaitTimeSeconds(2);

        assertThat(waitTime).isZero();
        assertThat(requestQueue.size()).isEqualTo(1);
    }

    @Test
    void resetClearsAllCounters() {
        assertThat(rateLimiter.tryAcquire(1, 1)).isTrue();
        assertThat(rateLimiter.getCurrentRpm()).isEqualTo(1);
        assertThat(rateLimiter.getCurrentRpd()).isEqualTo(1);

        rateLimiter.reset();

        assertThat(rateLimiter.getCurrentRpm()).isZero();
        assertThat(rateLimiter.getCurrentRpd()).isZero();
    }

    @SuppressWarnings("unchecked")
    private Queue<Instant> getRequestQueue() {
        return (Queue<Instant>) Objects.requireNonNull(
                ReflectionTestUtils.getField(rateLimiter, "requestTimestamps"));
    }
}
