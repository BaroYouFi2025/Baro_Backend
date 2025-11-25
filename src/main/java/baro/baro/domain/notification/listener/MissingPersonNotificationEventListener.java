package baro.baro.domain.notification.listener;

import baro.baro.domain.notification.dto.event.MissingPersonFoundNotificationEvent;
import baro.baro.domain.notification.dto.event.NearbyAlertNotificationEvent;
import baro.baro.domain.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 실종자 관련 푸시 알림을 트랜잭션 커밋 이후 비동기로 발송하기 위한 이벤트 리스너
@Slf4j
@Component
@RequiredArgsConstructor
public class MissingPersonNotificationEventListener {

    private final PushNotificationService pushNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMissingPersonFound(MissingPersonFoundNotificationEvent event) {
        try {
            pushNotificationService.sendMissingPersonFoundNotification(
                    event.getSightingId(),
                    event.getMissingPersonOwner(),
                    event.getMissingPersonName(),
                    event.getReporterName(),
                    event.getAddress()
            );
        } catch (Exception e) {
            log.error("실종자 발견 알림 발송 실패 - sightingId: {}", event.getSightingId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNearbyAlert(NearbyAlertNotificationEvent event) {
        try {
            pushNotificationService.sendNearbyAlertToReporter(
                    event.getReporter(),
                    event.getMissingPersonName(),
                    event.getDistance(),
                    event.getReporterLocation(),
                    event.getMissingPersonId()
            );
        } catch (Exception e) {
            log.error("NEARBY_ALERT 알림 발송 실패 - reporterId: {}, missingPersonId: {}",
                    event.getReporter().getId(), event.getMissingPersonId(), e);
        }
    }
}
