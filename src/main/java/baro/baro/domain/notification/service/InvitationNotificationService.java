package baro.baro.domain.notification.service;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.user.entity.User;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 초대 알림 서비스
//
// 구성원 초대 요청 및 응답 알림을 처리합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationNotificationService {

    private final NotificationDeviceService notificationDeviceService;
    private final FcmDispatchService fcmDispatchService;
    private final NotificationPersistenceService persistenceService;

    // 초대 요청 푸시 알림을 발송합니다.
    @Transactional
    public void sendInvitationNotification(User invitee, User inviter, String relation, Long invitationId) {
        String title = "새로운 구성원 초대 요청";
        String message = String.format("%s님이 %s로 초대 요청을 보냈습니다.", inviter.getName(), relation);

        persistenceService.save(invitee, NotificationType.INVITE_REQUEST, title, message, invitationId);

        List<Device> devices = notificationDeviceService.getActiveDevicesWithToken(invitee);
        if (devices.isEmpty()) {
            log.warn("초대받은 사용자 {}의 활성 기기가 없습니다. 앱내 알림만 저장됩니다.", invitee.getName());
            return;
        }

        for (Device device : devices) {
            Message fcmMessage = fcmDispatchService.buildInvitationMessage(
                    device.getFcmToken(), title, message, invitationId, inviter.getName(), relation
            );
            fcmDispatchService.dispatch(fcmMessage, "invitation", device.getFcmToken());
        }

        log.info("초대 요청 알림 발송 완료 - 초대받은 사용자: {}, 초대한 사용자: {}", invitee.getName(), inviter.getName());
    }

    // 초대 응답 푸시 알림을 발송합니다.
    @Transactional
    public void sendInvitationResponseNotification(User inviter, User invitee, boolean isAccepted, String relation) {
        String title = isAccepted ? "초대 요청이 수락되었습니다" : "초대 요청이 거절되었습니다";
        String message = String.format("%s님이 %s 초대 요청을 %s했습니다.",
                invitee.getName(), relation, isAccepted ? "수락" : "거절");

        persistenceService.save(inviter, NotificationType.INVITE_REQUEST, title, message, null);

        List<Device> devices = notificationDeviceService.getActiveDevicesWithToken(inviter);
        if (devices.isEmpty()) {
            log.warn("초대한 사용자 {}의 활성 기기가 없습니다. 앱내 알림만 저장됩니다.", inviter.getName());
            return;
        }

        String notificationType = isAccepted ? "invitation_accepted" : "invitation_rejected";
        for (Device device : devices) {
            Message fcmMessage = fcmDispatchService.buildInvitationResponseMessage(
                    device.getFcmToken(), title, message, notificationType,
                    invitee.getName(), relation, isAccepted
            );
            fcmDispatchService.dispatch(fcmMessage, notificationType, device.getFcmToken());
        }

        log.info("초대 응답 알림 발송 완료 - 초대한 사용자: {}, 수락여부: {}", inviter.getName(), isAccepted);
    }

}
