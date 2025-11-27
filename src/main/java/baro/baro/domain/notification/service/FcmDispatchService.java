package baro.baro.domain.notification.service;

import baro.baro.domain.common.monitoring.MetricsService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// FCM 메시지 전송 서비스
//
// Firebase Cloud Messaging을 통해 푸시 알림을 전송합니다.
// 메트릭 수집 및 오류 처리를 담당합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmDispatchService {

    private final MetricsService metricsService;

    // FCM 메시지를 전송합니다.
    //
    // @param message 전송할 FCM 메시지
    // @param notificationType 알림 타입 (메트릭용)
    // @param fcmToken FCM 토큰 (로깅용)
    @SneakyThrows(FirebaseMessagingException.class)
    public void dispatch(Message message, String notificationType, String fcmToken) {
        long startTime = System.currentTimeMillis();

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase가 초기화되지 않았습니다. Firebase 설정을 확인해주세요.");
            metricsService.recordFcmMessageFailure(notificationType, "FIREBASE_NOT_INITIALIZED");
            return;
        }

        String response = FirebaseMessaging.getInstance().send(message);
        log.info("FCM 푸시 알림 발송 성공 - 타입: {}, 토큰: {}, 응답: {}", notificationType, fcmToken, response);

        metricsService.recordFcmMessageSuccess(notificationType);
        metricsService.recordFcmSendDuration(System.currentTimeMillis() - startTime);
    }

    // 초대 요청 FCM 메시지를 생성합니다.
    public Message buildInvitationMessage(String fcmToken, String title, String message,
                                          Long invitationId, String inviterName, String relation) {
        return Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .build())
                .putData("type", "invitation")
                .putData("invitationId", String.valueOf(invitationId))
                .putData("inviterName", inviterName)
                .putData("relation", relation)
                .putData("actions", "[\"accept\", \"reject\"]")
                .build();
    }

    // 초대 응답 FCM 메시지를 생성합니다.
    public Message buildInvitationResponseMessage(String fcmToken, String title, String message,
                                                   String type, String inviteeName, String relation,
                                                   boolean isAccepted) {
        return Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .build())
                .putData("type", type)
                .putData("inviteeName", inviteeName)
                .putData("relation", relation)
                .putData("isAccepted", String.valueOf(isAccepted))
                .build();
    }

    // 실종자 발견 FCM 메시지를 생성합니다.
    public Message buildFoundMessage(String fcmToken, String title, String message,
                                      String missingPersonName, String location) {
        return Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .build())
                .putData("type", "found")
                .putData("missingPersonName", missingPersonName)
                .putData("location", location)
                .putData("deepLink", "youfi://found?name=" + missingPersonName)
                .build();
    }

    // 실종자 발견 신고 FCM 메시지를 생성합니다.
    public Message buildMissingPersonFoundMessage(String fcmToken, String title, String message,
                                                   String missingPersonName, String reporterName,
                                                   String address) {
        return Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .build())
                .putData("type", "missing_person_found")
                .putData("missingPersonName", missingPersonName)
                .putData("reporterName", reporterName)
                .putData("address", address != null ? address : "위치 정보 없음")
                .putData("deepLink", "youfi://missing-person-found?name=" + missingPersonName)
                .build();
    }

    // NEARBY_ALERT FCM 메시지를 생성합니다.
    public Message buildNearbyAlertMessage(String fcmToken, String title, String message,
                                            String missingPersonName, String reporterName,
                                            double distance, Long missingPersonId,
                                            String recipientType) {
        return Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .build())
                .putData("type", "nearby_alert")
                .putData("missingPersonName", missingPersonName)
                .putData("reporterName", reporterName)
                .putData("distance", String.valueOf(distance))
                .putData("missingPersonId", String.valueOf(missingPersonId))
                .putData("recipientType", recipientType)
                .putData("deepLink", "youfi://nearby-alert?id=" + missingPersonId)
                .build();
    }
}
