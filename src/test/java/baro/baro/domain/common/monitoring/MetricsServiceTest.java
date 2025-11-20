package baro.baro.domain.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MetricsService 테스트")
class MetricsServiceTest {

    private MetricsService metricsService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    // ==================== 실종자 관련 메트릭 테스트 ====================

    @Test
    @DisplayName("실종자 신고 메트릭 기록 - Counter 증가 확인")
    void recordMissingPersonReport() {
        // when
        metricsService.recordMissingPersonReport();

        // then
        Counter counter = meterRegistry.find("missing_person_reports_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
        assertThat(counter.getId().getTag("type")).isEqualTo("new");
    }

    @Test
    @DisplayName("실종자 신고 메트릭 여러 번 기록")
    void recordMissingPersonReport_multiple() {
        // when
        metricsService.recordMissingPersonReport();
        metricsService.recordMissingPersonReport();
        metricsService.recordMissingPersonReport();

        // then
        Counter counter = meterRegistry.find("missing_person_reports_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("실종자 발견 메트릭 기록 - Counter 증가 확인")
    void recordMissingPersonFound() {
        // when
        metricsService.recordMissingPersonFound();

        // then
        Counter counter = meterRegistry.find("missing_person_found_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
        assertThat(counter.getId().getTag("status")).isEqualTo("found");
    }

    @Test
    @DisplayName("실종자 발견 메트릭 여러 번 기록")
    void recordMissingPersonFound_multiple() {
        // when
        metricsService.recordMissingPersonFound();
        metricsService.recordMissingPersonFound();

        // then
        Counter counter = meterRegistry.find("missing_person_found_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    // ==================== GPS 위치 추적 메트릭 테스트 ====================

    @Test
    @DisplayName("GPS 위치 업데이트 메트릭 기록")
    void recordGpsLocationUpdate() {
        // when
        metricsService.recordGpsLocationUpdate();

        // then
        Counter counter = meterRegistry.find("gps_location_updates_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("GPS 위치 업데이트 메트릭 여러 번 기록")
    void recordGpsLocationUpdate_multiple() {
        // when
        for (int i = 0; i < 10; i++) {
            metricsService.recordGpsLocationUpdate();
        }

        // then
        Counter counter = meterRegistry.find("gps_location_updates_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("GPS 업데이트 소요 시간 기록")
    void recordGpsUpdateDuration() {
        // given
        long durationMs = 150L;

        // when
        metricsService.recordGpsUpdateDuration(durationMs);

        // then
        Timer timer = meterRegistry.find("gps_update_duration_seconds").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(durationMs);
    }

    @Test
    @DisplayName("GPS 업데이트 소요 시간 여러 번 기록")
    void recordGpsUpdateDuration_multiple() {
        // when
        metricsService.recordGpsUpdateDuration(100L);
        metricsService.recordGpsUpdateDuration(200L);
        metricsService.recordGpsUpdateDuration(300L);

        // then
        Timer timer = meterRegistry.find("gps_update_duration_seconds").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(3L);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(600L);
    }

    // ==================== AI 이미지 생성 메트릭 테스트 ====================

    @Test
    @DisplayName("AI 이미지 생성 성공 메트릭 기록")
    void recordAiImageGenerationSuccess() {
        // given
        String assetType = "AGE_PROGRESSION";

        // when
        metricsService.recordAiImageGenerationSuccess(assetType);

        // then
        Counter counter = meterRegistry.find("ai_image_generation_success_total")
                .tag("asset_type", assetType)
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("AI 이미지 생성 성공 메트릭 - 다양한 타입")
    void recordAiImageGenerationSuccess_variousTypes() {
        // when
        metricsService.recordAiImageGenerationSuccess("AGE_PROGRESSION");
        metricsService.recordAiImageGenerationSuccess("AGE_PROGRESSION");
        metricsService.recordAiImageGenerationSuccess("GENERATED_IMAGE");

        // then
        Counter ageProgressionCounter = meterRegistry.find("ai_image_generation_success_total")
                .tag("asset_type", "AGE_PROGRESSION")
                .counter();
        assertThat(ageProgressionCounter).isNotNull();
        assertThat(ageProgressionCounter.count()).isEqualTo(2.0);

        Counter generatedImageCounter = meterRegistry.find("ai_image_generation_success_total")
                .tag("asset_type", "GENERATED_IMAGE")
                .counter();
        assertThat(generatedImageCounter).isNotNull();
        assertThat(generatedImageCounter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("AI 이미지 생성 실패 메트릭 기록")
    void recordAiImageGenerationFailure() {
        // given
        String assetType = "AGE_PROGRESSION";
        String errorType = "RATE_LIMIT_EXCEEDED";

        // when
        metricsService.recordAiImageGenerationFailure(assetType, errorType);

        // then
        Counter counter = meterRegistry.find("ai_image_generation_failure_total")
                .tag("asset_type", assetType)
                .tag("error_type", errorType)
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("AI 이미지 생성 실패 메트릭 - 다양한 에러 타입")
    void recordAiImageGenerationFailure_variousErrors() {
        // when
        metricsService.recordAiImageGenerationFailure("AGE_PROGRESSION", "RATE_LIMIT_EXCEEDED");
        metricsService.recordAiImageGenerationFailure("AGE_PROGRESSION", "API_ERROR");
        metricsService.recordAiImageGenerationFailure("GENERATED_IMAGE", "INVALID_RESPONSE");

        // then
        Counter rateLimitCounter = meterRegistry.find("ai_image_generation_failure_total")
                .tag("asset_type", "AGE_PROGRESSION")
                .tag("error_type", "RATE_LIMIT_EXCEEDED")
                .counter();
        assertThat(rateLimitCounter).isNotNull();
        assertThat(rateLimitCounter.count()).isEqualTo(1.0);

        Counter apiErrorCounter = meterRegistry.find("ai_image_generation_failure_total")
                .tag("asset_type", "AGE_PROGRESSION")
                .tag("error_type", "API_ERROR")
                .counter();
        assertThat(apiErrorCounter).isNotNull();
        assertThat(apiErrorCounter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("AI 이미지 생성 소요 시간 기록")
    void recordAiGenerationDuration() {
        // given
        long durationMs = 3500L;
        String assetType = "AGE_PROGRESSION";

        // when
        metricsService.recordAiGenerationDuration(durationMs, assetType);

        // then
        Timer timer = meterRegistry.find("ai_generation_duration_seconds")
                .tag("asset_type", assetType)
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(durationMs);
    }

    @Test
    @DisplayName("AI 이미지 생성 소요 시간 - 다양한 타입별 기록")
    void recordAiGenerationDuration_variousTypes() {
        // when
        metricsService.recordAiGenerationDuration(2000L, "AGE_PROGRESSION");
        metricsService.recordAiGenerationDuration(1500L, "AGE_PROGRESSION");
        metricsService.recordAiGenerationDuration(3000L, "GENERATED_IMAGE");

        // then
        Timer ageProgressionTimer = meterRegistry.find("ai_generation_duration_seconds")
                .tag("asset_type", "AGE_PROGRESSION")
                .timer();
        assertThat(ageProgressionTimer).isNotNull();
        assertThat(ageProgressionTimer.count()).isEqualTo(2L);
        assertThat(ageProgressionTimer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(3500L);

        Timer generatedImageTimer = meterRegistry.find("ai_generation_duration_seconds")
                .tag("asset_type", "GENERATED_IMAGE")
                .timer();
        assertThat(generatedImageTimer).isNotNull();
        assertThat(generatedImageTimer.count()).isEqualTo(1L);
        assertThat(generatedImageTimer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(3000L);
    }

    // ==================== FCM 푸시 알림 메트릭 테스트 ====================

    @Test
    @DisplayName("FCM 메시지 전송 성공 메트릭 기록")
    void recordFcmMessageSuccess() {
        // given
        String notificationType = "NEARBY_ALERT";

        // when
        metricsService.recordFcmMessageSuccess(notificationType);

        // then
        Counter counter = meterRegistry.find("fcm_messages_success_total")
                .tag("notification_type", notificationType)
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("FCM 메시지 전송 성공 - 다양한 알림 타입")
    void recordFcmMessageSuccess_variousTypes() {
        // when
        metricsService.recordFcmMessageSuccess("NEARBY_ALERT");
        metricsService.recordFcmMessageSuccess("NEARBY_ALERT");
        metricsService.recordFcmMessageSuccess("MISSING_PERSON_REPORT");
        metricsService.recordFcmMessageSuccess("INVITE_REQUEST");

        // then
        Counter nearbyCounter = meterRegistry.find("fcm_messages_success_total")
                .tag("notification_type", "NEARBY_ALERT")
                .counter();
        assertThat(nearbyCounter).isNotNull();
        assertThat(nearbyCounter.count()).isEqualTo(2.0);

        Counter reportCounter = meterRegistry.find("fcm_messages_success_total")
                .tag("notification_type", "MISSING_PERSON_REPORT")
                .counter();
        assertThat(reportCounter).isNotNull();
        assertThat(reportCounter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("FCM 메시지 전송 실패 메트릭 기록")
    void recordFcmMessageFailure() {
        // given
        String notificationType = "NEARBY_ALERT";
        String errorType = "INVALID_TOKEN";

        // when
        metricsService.recordFcmMessageFailure(notificationType, errorType);

        // then
        Counter counter = meterRegistry.find("fcm_messages_failure_total")
                .tag("notification_type", notificationType)
                .tag("error_type", errorType)
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("FCM 메시지 전송 실패 - 다양한 에러 타입")
    void recordFcmMessageFailure_variousErrors() {
        // when
        metricsService.recordFcmMessageFailure("NEARBY_ALERT", "INVALID_TOKEN");
        metricsService.recordFcmMessageFailure("NEARBY_ALERT", "NETWORK_ERROR");
        metricsService.recordFcmMessageFailure("INVITE_REQUEST", "INVALID_TOKEN");

        // then
        Counter invalidTokenCounter = meterRegistry.find("fcm_messages_failure_total")
                .tag("notification_type", "NEARBY_ALERT")
                .tag("error_type", "INVALID_TOKEN")
                .counter();
        assertThat(invalidTokenCounter).isNotNull();
        assertThat(invalidTokenCounter.count()).isEqualTo(1.0);

        Counter networkErrorCounter = meterRegistry.find("fcm_messages_failure_total")
                .tag("notification_type", "NEARBY_ALERT")
                .tag("error_type", "NETWORK_ERROR")
                .counter();
        assertThat(networkErrorCounter).isNotNull();
        assertThat(networkErrorCounter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("FCM 메시지 전송 소요 시간 기록")
    void recordFcmSendDuration() {
        // given
        long durationMs = 250L;

        // when
        metricsService.recordFcmSendDuration(durationMs);

        // then
        Timer timer = meterRegistry.find("fcm_send_duration_seconds").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(durationMs);
    }

    @Test
    @DisplayName("FCM 메시지 전송 소요 시간 여러 번 기록")
    void recordFcmSendDuration_multiple() {
        // when
        metricsService.recordFcmSendDuration(100L);
        metricsService.recordFcmSendDuration(150L);
        metricsService.recordFcmSendDuration(200L);

        // then
        Timer timer = meterRegistry.find("fcm_send_duration_seconds").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(3L);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(450L);
    }

    // ==================== 사용자 활동 메트릭 테스트 ====================

    @Test
    @DisplayName("사용자 로그인 메트릭 기록")
    void recordUserLogin() {
        // when
        metricsService.recordUserLogin();

        // then
        Counter counter = meterRegistry.find("user_logins_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("사용자 로그인 메트릭 여러 번 기록")
    void recordUserLogin_multiple() {
        // when
        for (int i = 0; i < 5; i++) {
            metricsService.recordUserLogin();
        }

        // then
        Counter counter = meterRegistry.find("user_logins_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("사용자 등록 메트릭 기록")
    void recordUserRegistration() {
        // when
        metricsService.recordUserRegistration();

        // then
        Counter counter = meterRegistry.find("user_registrations_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("사용자 등록 메트릭 여러 번 기록")
    void recordUserRegistration_multiple() {
        // when
        for (int i = 0; i < 3; i++) {
            metricsService.recordUserRegistration();
        }

        // then
        Counter counter = meterRegistry.find("user_registrations_total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    // ==================== 에러 메트릭 테스트 ====================

    @Test
    @DisplayName("HTTP 에러 메트릭 기록")
    void recordHttpError() {
        // given
        String errorType = "USER_NOT_FOUND";
        int statusCode = 404;

        // when
        metricsService.recordHttpError(errorType, statusCode);

        // then
        Counter counter = meterRegistry.find("http_errors_total")
                .tag("error_type", errorType)
                .tag("status", String.valueOf(statusCode))
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("HTTP 에러 메트릭 - 다양한 상태 코드")
    void recordHttpError_variousStatusCodes() {
        // when
        metricsService.recordHttpError("BAD_REQUEST", 400);
        metricsService.recordHttpError("UNAUTHORIZED", 401);
        metricsService.recordHttpError("NOT_FOUND", 404);
        metricsService.recordHttpError("INTERNAL_SERVER_ERROR", 500);

        // then
        Counter badRequestCounter = meterRegistry.find("http_errors_total")
                .tag("error_type", "BAD_REQUEST")
                .tag("status", "400")
                .counter();
        assertThat(badRequestCounter).isNotNull();
        assertThat(badRequestCounter.count()).isEqualTo(1.0);

        Counter unauthorizedCounter = meterRegistry.find("http_errors_total")
                .tag("error_type", "UNAUTHORIZED")
                .tag("status", "401")
                .counter();
        assertThat(unauthorizedCounter).isNotNull();
        assertThat(unauthorizedCounter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("동일한 에러 타입 여러 번 기록")
    void recordHttpError_sameTypeMultipleTimes() {
        // when
        metricsService.recordHttpError("VALIDATION_ERROR", 400);
        metricsService.recordHttpError("VALIDATION_ERROR", 400);
        metricsService.recordHttpError("VALIDATION_ERROR", 400);

        // then
        Counter counter = meterRegistry.find("http_errors_total")
                .tag("error_type", "VALIDATION_ERROR")
                .tag("status", "400")
                .counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    // ==================== 통합 시나리오 테스트 ====================

    @Test
    @DisplayName("통합 시나리오 - 실종자 신고부터 발견까지 메트릭 추적")
    void integrationScenario_missingPersonLifecycle() {
        // 실종자 신고
        metricsService.recordMissingPersonReport();
        metricsService.recordMissingPersonReport();

        // GPS 위치 업데이트
        metricsService.recordGpsLocationUpdate();
        metricsService.recordGpsUpdateDuration(100L);

        // AI 이미지 생성
        metricsService.recordAiImageGenerationSuccess("AGE_PROGRESSION");
        metricsService.recordAiGenerationDuration(2000L, "AGE_PROGRESSION");

        // FCM 알림 전송
        metricsService.recordFcmMessageSuccess("NEARBY_ALERT");
        metricsService.recordFcmSendDuration(150L);

        // 실종자 발견
        metricsService.recordMissingPersonFound();

        // then - 모든 메트릭이 정상 기록되었는지 확인
        assertThat(meterRegistry.find("missing_person_reports_total").counter().count()).isEqualTo(2.0);
        assertThat(meterRegistry.find("gps_location_updates_total").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("ai_image_generation_success_total").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("fcm_messages_success_total").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.find("missing_person_found_total").counter().count()).isEqualTo(1.0);
    }
}
