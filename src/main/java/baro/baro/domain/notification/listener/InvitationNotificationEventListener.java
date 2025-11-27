package baro.baro.domain.notification.listener;

import baro.baro.domain.member.dto.event.InvitationCreatedEvent;
import baro.baro.domain.member.dto.event.InvitationResponseEvent;
import baro.baro.domain.notification.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 초대 관련 푸시 알림을 트랜잭션 커밋 이후에 발송하기 위한 이벤트 리스너
@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationNotificationEventListener {

    private final PushNotificationService pushNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationCreated(InvitationCreatedEvent event) {
        try {
            pushNotificationService.sendInvitationNotification(
                    event.getInvitee(), event.getInviter(), event.getRelation(), event.getInvitationId());
        } catch (Exception e) {
            log.error("초대 알림 발송 실패 - invitationId: {}", event.getInvitationId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationResponded(InvitationResponseEvent event) {
        try {
            pushNotificationService.sendInvitationResponseNotification(
                    event.getInviter(), event.getInvitee(), event.isAccepted(), event.getRelation());
        } catch (Exception e) {
            log.error("초대 응답 알림 발송 실패 - inviterId: {}, inviteeId: {}", 
                    event.getInviter().getId(), event.getInvitee().getId(), e);
        }
    }
}
