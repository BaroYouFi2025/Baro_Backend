package baro.baro.domain.notification.service;

import baro.baro.domain.notification.entity.Notification;
import baro.baro.domain.notification.repository.NotificationRepository;
import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.notification.dto.NotificationResponse;
import baro.baro.domain.notification.exception.NotificationErrorCode;
import baro.baro.domain.notification.exception.NotificationException;
import baro.baro.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 알림 서비스 - 사용자의 알림 조회 및 관리
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 현재 로그인한 사용자의 모든 알림 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        User currentUser = SecurityUtil.getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(currentUser);

        log.info("사용자 {}의 알림 조회 - 총 {}건", currentUser.getName(), notifications.size());

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    // 현재 로그인한 사용자의 읽지 않은 알림만 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {
        User currentUser = SecurityUtil.getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(currentUser);

        log.info("사용자 {}의 읽지 않은 알림 조회 - 총 {}건", currentUser.getName(), notifications.size());

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    // 특정 사용자 ID의 모든 알림 조회 (본인만 가능)
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        User user = SecurityUtil.getCurrentUser();

        // 현재 사용자의 ID와 요청한 userId가 같은지 확인
        if (!user.getId().equals(userId)) {
            log.warn("권한 없는 알림 조회 시도 - 현재 사용자: {}, 요청 사용자 ID: {}", user.getId(), userId);
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_OWNED_BY_USER);
        }

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        log.info("사용자 ID {}의 알림 조회 - 총 {}건", userId, notifications.size());

        return notifications.stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    // 현재 로그인한 사용자의 읽지 않은 알림 개수 조회
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = SecurityUtil.getCurrentUser();
        long unreadCount = notificationRepository.countUnreadByUser(currentUser);

        log.info("사용자 {}의 읽지 않은 알림 개수: {}", currentUser.getName(), unreadCount);

        return unreadCount;
    }

    // 알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId) {
        User currentUser = SecurityUtil.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        // 현재 사용자의 알림인지 확인
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            log.warn("권한 없는 알림 읽음 처리 시도 - 현재 사용자: {}, 알림 사용자: {}",
                    currentUser.getId(), notification.getUser().getId());
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_OWNED_BY_USER);
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        log.info("알림 읽음 처리 완료 - 알림 ID: {}, 사용자: {}", notificationId, currentUser.getName());
    }
}
