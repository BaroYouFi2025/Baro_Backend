package baro.baro.domain.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 커스텀 비즈니스 메트릭 수집 서비스
// Prometheus + Grafana로 모니터링되는 도메인 메트릭을 관리합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // ==================== 실종자 관련 메트릭 ====================

    // 실종자 신고 등록 메트릭 기록
    public void recordMissingPersonReport() {
        Counter.builder("missing_person_reports_total")
                .description("총 실종자 신고 수")
                .tag("type", "new")
                .register(meterRegistry)
                .increment();
        log.debug("Metric recorded: missing_person_reports_total");
    }

    // 실종자 발견 메트릭 기록
    public void recordMissingPersonFound() {
        Counter.builder("missing_person_found_total")
                .description("실종자 발견 수")
                .tag("status", "found")
                .register(meterRegistry)
                .increment();
        log.debug("Metric recorded: missing_person_found_total");
    }

    // ==================== GPS 위치 추적 메트릭 ====================

    // GPS 위치 업데이트 메트릭 기록
    public void recordGpsLocationUpdate() {
        Counter.builder("gps_location_updates_total")
                .description("GPS 위치 업데이트 총 수")
                .register(meterRegistry)
                .increment();
    }

    // GPS 위치 업데이트 소요 시간 기록
    // @param duration 소요 시간 (밀리초)
    public void recordGpsUpdateDuration(long duration) {
        Timer.builder("gps_update_duration_seconds")
                .description("GPS 위치 업데이트 소요 시간")
                .register(meterRegistry)
                .record(java.time.Duration.ofMillis(duration));
    }

    // ==================== AI 이미지 생성 메트릭 ====================

    // AI 이미지 생성 성공 메트릭 기록
    // @param assetType AI 생성 타입 (AGE_PROGRESSION, GENERATED_IMAGE)
    public void recordAiImageGenerationSuccess(String assetType) {
        Counter.builder("ai_image_generation_success_total")
                .description("AI 이미지 생성 성공 수")
                .tag("asset_type", assetType)
                .register(meterRegistry)
                .increment();
        log.debug("Metric recorded: ai_image_generation_success - type: {}", assetType);
    }

    // AI 이미지 생성 실패 메트릭 기록
    // @param assetType AI 생성 타입
    // @param errorType 에러 타입
    public void recordAiImageGenerationFailure(String assetType, String errorType) {
        Counter.builder("ai_image_generation_failure_total")
                .description("AI 이미지 생성 실패 수")
                .tag("asset_type", assetType)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
        log.warn("Metric recorded: ai_image_generation_failure - type: {}, error: {}", assetType, errorType);
    }

    // AI 이미지 생성 소요 시간 기록
    // @param duration 소요 시간 (밀리초)
    // @param assetType AI 생성 타입
    public void recordAiGenerationDuration(long duration, String assetType) {
        Timer.builder("ai_generation_duration_seconds")
                .description("AI 이미지 생성 소요 시간")
                .tag("asset_type", assetType)
                .register(meterRegistry)
                .record(java.time.Duration.ofMillis(duration));
    }

    // ==================== FCM 푸시 알림 메트릭 ====================

    // FCM 메시지 전송 성공 메트릭 기록
    // @param notificationType 알림 타입 (NEARBY_ALERT, MISSING_PERSON_REPORT, etc.)
    public void recordFcmMessageSuccess(String notificationType) {
        Counter.builder("fcm_messages_success_total")
                .description("FCM 메시지 전송 성공 수")
                .tag("notification_type", notificationType)
                .register(meterRegistry)
                .increment();
        log.debug("Metric recorded: fcm_messages_success - type: {}", notificationType);
    }

    // FCM 메시지 전송 실패 메트릭 기록
    // @param notificationType 알림 타입
    // @param errorType 에러 타입 (INVALID_TOKEN, NETWORK_ERROR, etc.)
    public void recordFcmMessageFailure(String notificationType, String errorType) {
        Counter.builder("fcm_messages_failure_total")
                .description("FCM 메시지 전송 실패 수")
                .tag("notification_type", notificationType)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
        log.warn("Metric recorded: fcm_messages_failure - type: {}, error: {}", notificationType, errorType);
    }

    // FCM 메시지 전송 소요 시간 기록
    // @param duration 소요 시간 (밀리초)
    public void recordFcmSendDuration(long duration) {
        Timer.builder("fcm_send_duration_seconds")
                .description("FCM 메시지 전송 소요 시간")
                .register(meterRegistry)
                .record(java.time.Duration.ofMillis(duration));
    }

    // ==================== 사용자 활동 메트릭 ====================

    // 사용자 로그인 메트릭 기록
    public void recordUserLogin() {
        Counter.builder("user_logins_total")
                .description("사용자 로그인 총 수")
                .register(meterRegistry)
                .increment();
    }

    // 사용자 등록 메트릭 기록
    public void recordUserRegistration() {
        Counter.builder("user_registrations_total")
                .description("사용자 등록 총 수")
                .register(meterRegistry)
                .increment();
    }

    // ==================== 에러 메트릭 ====================

    // 전역 예외 처리기에 의해 기록되는 HTTP 에러 메트릭
    //
    // @param errorType  예외 또는 에러 코드
    // @param statusCode HTTP 상태 코드
    public void recordHttpError(String errorType, int statusCode) {
        Counter.builder("http_errors_total")
                .description("전역 예외 처리기에서 수집된 HTTP 에러 수")
                .tag("error_type", errorType)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry)
                .increment();
    }
}
