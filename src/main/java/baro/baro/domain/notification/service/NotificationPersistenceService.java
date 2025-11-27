package baro.baro.domain.notification.service;

import baro.baro.domain.notification.entity.Notification;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.notification.repository.NotificationRepository;
import baro.baro.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// 알림 저장 서비스
//
// 알림 이력을 데이터베이스에 저장합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPersistenceService {

    private final NotificationRepository notificationRepository;

    // 알림 이력을 데이터베이스에 저장합니다.
    //
    // @param user 수신 사용자
    // @param type 알림 타입
    // @param title 제목
    // @param message 내용
    // @param relatedEntityId 관련 엔티티 ID (선택)
    public void save(User user, NotificationType type, String title, String message, Long relatedEntityId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("알림 이력 저장 완료 - 사용자: {}, 타입: {}", user.getName(), type);
    }

    // 위치 정보를 포함한 알림 이력을 데이터베이스에 저장합니다.
    //
    // @param user 수신 사용자
    // @param type 알림 타입
    // @param title 제목
    // @param message 내용
    // @param relatedEntityId 관련 엔티티 ID
    // @param location 관련 위치
    public void saveWithLocation(User user, NotificationType type, String title, String message,
                                  Long relatedEntityId, Point location) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .relatedEntityId(relatedEntityId)
                .relatedLocation(location)
                .build();

        notificationRepository.save(notification);
        log.info("알림 이력 저장 완료 (위치 포함) - 사용자: {}, 타입: {}, 관련 ID: {}",
                 user.getName(), type, relatedEntityId);
    }
}
