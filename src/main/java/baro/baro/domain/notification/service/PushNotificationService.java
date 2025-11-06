package baro.baro.domain.notification.service;

import baro.baro.domain.common.entity.Notification;
import baro.baro.domain.common.enums.NotificationType;
import baro.baro.domain.common.repository.NotificationRepository;
import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.user.entity.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 푸시 알림 서비스
 * 
 * Firebase Cloud Messaging을 사용하여 푸시 알림을 발송하고
 * 알림 이력을 데이터베이스에 저장합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final DeviceRepository deviceRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 초대 요청 푸시 알림을 발송합니다.
     *
     * @param invitee  초대받은 사용자
     * @param inviter  초대한 사용자
     * @param relation 관계 (예: 아들, 딸, 아버지, 어머니)
     */
    @Transactional
    public void sendInvitationNotification(User invitee, User inviter, String relation) {
        try {
            // 1. 초대받은 사용자의 활성 기기들 조회
            List<Device> devices = deviceRepository.findByUser(invitee).stream()
                    .filter(device -> device.isActive())
                    .filter(device -> device.getFcmToken() != null && !device.getFcmToken().isEmpty())
                    .toList();

            if (devices.isEmpty()) {
                log.warn("초대받은 사용자 {}의 활성 기기 또는 FCM 토큰이 없습니다.", invitee.getName());
                return;
            }

            // 2. 알림 메시지 생성
            String title = "새로운 구성원 초대 요청";
            String message = String.format("%s님이 %s로 초대 요청을 보냈습니다.", inviter.getName(), relation);

            // 3. 각 기기에 푸시 알림 발송
            for (Device device : devices) {
                sendPushNotification(device.getFcmToken(), title, message);
            }

            // 4. 알림 이력을 데이터베이스에 저장
            saveNotification(invitee, NotificationType.INVITE_REQUEST, title, message);

            log.info("초대 요청 푸시 알림 발송 완료 - 초대받은 사용자: {}, 초대한 사용자: {}",
                    invitee.getName(), inviter.getName());

        } catch (Exception e) {
            log.error("초대 요청 푸시 알림 발송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 초대 응답 푸시 알림을 발송합니다.
     *
     * @param inviter    초대한 사용자
     * @param invitee    초대받은 사용자
     * @param isAccepted 수락 여부
     * @param relation   관계
     */
    @Transactional
    public void sendInvitationResponseNotification(User inviter, User invitee, boolean isAccepted, String relation) {
        try {
            // 1. 초대한 사용자의 활성 기기들 조회
            List<Device> devices = deviceRepository.findByUser(inviter).stream()
                    .filter(device -> device.isActive())
                    .filter(device -> device.getFcmToken() != null && !device.getFcmToken().isEmpty())
                    .toList();

            if (devices.isEmpty()) {
                log.warn("초대한 사용자 {}의 활성 기기 또는 FCM 토큰이 없습니다.", inviter.getName());
                return;
            }

            // 2. 알림 메시지 생성
            String title = isAccepted ? "초대 요청이 수락되었습니다" : "초대 요청이 거절되었습니다";
            String message = String.format("%s님이 %s 초대 요청을 %s했습니다.",
                    invitee.getName(), relation, isAccepted ? "수락" : "거절");

            // 3. 각 기기에 푸시 알림 발송
            for (Device device : devices) {
                sendPushNotification(device.getFcmToken(), title, message);
            }

            // 4. 알림 이력을 데이터베이스에 저장
            saveNotification(inviter, NotificationType.INVITE_REQUEST, title, message);

            log.info("초대 응답 푸시 알림 발송 완료 - 초대한 사용자: {}, 초대받은 사용자: {}, 수락여부: {}",
                    inviter.getName(), invitee.getName(), isAccepted);

        } catch (Exception e) {
            log.error("초대 응답 푸시 알림 발송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 실종자 발견 신고 푸시 알림을 발송합니다.
     * 
     * @param reporter           신고자 (실종자를 발견한 사용자)
     * @param missingPersonOwner 실종자 등록자 (알림을 받을 사용자)
     * @param missingPersonName  실종자 이름
     * @param reporterName       신고자 이름
     * @param address            발견 위치 주소
     */
    @Transactional
    public void sendMissingPersonFoundNotification(
            User reporter,
            User missingPersonOwner,
            String missingPersonName,
            String reporterName,
            String address) {
        
        try {
            // 1. 실종자 등록자의 활성 기기들 조회
            List<Device> devices = deviceRepository.findByUser(missingPersonOwner).stream()
                    .filter(device -> device.isActive())
                    .filter(device -> device.getFcmToken() != null && !device.getFcmToken().isEmpty())
                    .toList();

            if (devices.isEmpty()) {
                log.warn("실종자 등록자 {}의 활성 기기 또는 FCM 토큰이 없습니다.", missingPersonOwner.getName());
                return;
            }

            // 2. 알림 메시지 생성
            String title = "실종자가 발견되었습니다!";
            String message = String.format("실종자 %s님이 발견되었습니다\n\n" +
                    "착은 팀: %s 님\n" +
                    "발견 위치: %s",
                    missingPersonName,
                    reporterName,
                    address != null ? address : "위치 정보 없음");

            // 3. 각 기기에 푸시 알림 발송
            for (Device device : devices) {
                sendPushNotification(device.getFcmToken(), title, message, "missing_person_found");
            }

            // 4. 알림 이력을 데이터베이스에 저장
            saveNotification(missingPersonOwner, NotificationType.FOUND_REPORT, title, message);

            log.info("실종자 발견 신고 푸시 알림 발송 완료 - 실종자: {}, 신고자: {}, 등록자: {}",
                    missingPersonName, reporterName, missingPersonOwner.getName());

        } catch (Exception e) {
            log.error("실종자 발견 신고 푸시 알림 발송 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * FCM을 통해 푸시 알림을 발송합니다.
     *
     * @param fcmToken FCM 토큰
     * @param title    알림 제목
     * @param message  알림 내용
     */
    private void sendPushNotification(String fcmToken, String title, String message) {
        sendPushNotification(fcmToken, title, message, "invitation");
    }

    /**
     * FCM을 통해 푸시 알림을 발송합니다. (타입 지정 가능)
     *
     * @param fcmToken FCM 토큰
     * @param title    알림 제목
     * @param message  알림 내용
     * @param type     알림 타입 (invitation, missing_person_found 등)
     */
    private void sendPushNotification(String fcmToken, String title, String message, String type) {
        try {
            // Firebase 앱이 초기화되지 않은 경우 초기화
            if (FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase가 초기화되지 않았습니다. Firebase 설정을 확인해주세요.");
                return;
            }

            // FCM 메시지 생성
            Message fcmMessage = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(message)
                            .build())
                    .putData("type", type)
                    .build();

            // 푸시 알림 발송
            String response = FirebaseMessaging.getInstance().send(fcmMessage);
            log.info("FCM 푸시 알림 발송 성공 - 타입: {}, 토큰: {}, 응답: {}", type, fcmToken, response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 푸시 알림 발송 실패 - 토큰: {}, 오류: {}", fcmToken, e.getMessage());
        } catch (Exception e) {
            log.error("푸시 알림 발송 중 예상치 못한 오류 - 토큰: {}, 오류: {}", fcmToken, e.getMessage(), e);
        }
    }

    /**
     * 알림 이력을 데이터베이스에 저장합니다.
     *
     * @param user    사용자
     * @param type    알림 타입
     * @param title   제목
     * @param message 내용
     */
    private void saveNotification(User user, NotificationType type, String title, String message) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
            log.info("알림 이력 저장 완료 - 사용자: {}, 타입: {}", user.getName(), type);

        } catch (Exception e) {
            log.error("알림 이력 저장 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
