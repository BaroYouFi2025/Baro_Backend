package baro.baro.domain.notification.service;

import baro.baro.domain.member.dto.request.AcceptInvitationRequest;
import baro.baro.domain.member.dto.request.RejectInvitationRequest;
import baro.baro.domain.member.dto.response.AcceptInvitationResponse;
import baro.baro.domain.member.service.MemberService;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.entity.Sighting;
import baro.baro.domain.missingperson.repository.SightingRepository;
import baro.baro.domain.missingperson.service.MissingPersonService;
import baro.baro.domain.missingperson.dto.res.SightingDetailResponse;
import baro.baro.domain.notification.entity.Notification;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.notification.repository.NotificationRepository;
import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.notification.dto.res.NotificationResponse;
import baro.baro.domain.notification.exception.NotificationErrorCode;
import baro.baro.domain.notification.exception.NotificationException;
import baro.baro.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 알림 서비스 구현체 - 사용자의 알림 조회 및 관리
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationServiceInterface {

    private final NotificationRepository notificationRepository;
    private final MemberService memberService;
    private final MissingPersonService missingPersonService;
    private final SightingRepository sightingRepository;

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

    // 알림을 통한 초대 수락
    @Transactional
    public AcceptInvitationResponse acceptInvitationFromNotification(Long notificationId, String relation) {
        User currentUser = SecurityUtil.getCurrentUser();
        Notification notification = getNotificationForAction(notificationId, currentUser, NotificationType.INVITE_REQUEST);

        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(notification.getRelatedEntityId());
        request.setRelation(relation);

        AcceptInvitationResponse response = memberService.acceptInvitation(request);

        // 알림을 읽음 처리
        notification.markAsRead();
        notificationRepository.save(notification);

        log.info("알림을 통한 초대 수락 완료 - 알림 ID: {}, 초대 ID: {}, 사용자: {}",
                notificationId, notification.getRelatedEntityId(), currentUser.getName());

        return response;
    }

    // 알림을 통한 초대 거절
    @Transactional
    public void rejectInvitationFromNotification(Long notificationId) {
        User currentUser = SecurityUtil.getCurrentUser();
        Notification notification = getNotificationForAction(notificationId, currentUser, NotificationType.INVITE_REQUEST);

        RejectInvitationRequest request = new RejectInvitationRequest();
        request.setRelationshipId(notification.getRelatedEntityId());

        memberService.rejectInvitation(request);

        // 알림을 읽음 처리
        notification.markAsRead();
        notificationRepository.save(notification);

        log.info("알림을 통한 초대 거절 완료 - 알림 ID: {}, 초대 ID: {}, 사용자: {}",
                notificationId, notification.getRelatedEntityId(), currentUser.getName());
    }

    // 알림을 통한 실종자 상세 조회
    @Transactional(readOnly = true)
    public MissingPersonDetailResponse getMissingPersonDetailFromNotification(Long notificationId) {
        User currentUser = SecurityUtil.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        // 현재 사용자의 알림인지 확인
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_OWNED_BY_USER);
        }

        // 실종자 관련 알림인지 확인 (FOUND_REPORT, NEARBY_ALERT)
        if (notification.getType() != NotificationType.FOUND_REPORT &&
            notification.getType() != NotificationType.NEARBY_ALERT) {
            throw new NotificationException(NotificationErrorCode.INVALID_NOTIFICATION_TYPE);
        }

        log.info("알림을 통한 실종자 상세 조회 - 알림 ID: {}, 실종자 ID: {}, 사용자: {}",
                notificationId, notification.getRelatedEntityId(), currentUser.getName());

        return missingPersonService.getMissingPersonDetail(notification.getRelatedEntityId());
    }

    // 알림을 통한 발견 신고 상세 조회
    @Transactional(readOnly = true)
    public SightingDetailResponse getSightingDetailFromNotification(Long notificationId) {
        User currentUser = SecurityUtil.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        // 현재 사용자의 알림인지 확인
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_OWNED_BY_USER);
        }

        // 발견 신고 알림인지 확인
        if (notification.getType() != NotificationType.FOUND_REPORT) {
            throw new NotificationException(NotificationErrorCode.INVALID_NOTIFICATION_TYPE);
        }

        Sighting sighting = sightingRepository.findById(notification.getRelatedEntityId())
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.SIGHTING_NOT_FOUND));

        log.info("알림을 통한 발견 신고 상세 조회 - 알림 ID: {}, 신고 ID: {}, 사용자: {}",
                notificationId, notification.getRelatedEntityId(), currentUser.getName());

        return SightingDetailResponse.from(sighting);
    }

    // 알림 액션을 위한 공통 검증 메서드
    private Notification getNotificationForAction(Long notificationId, User currentUser, NotificationType expectedType) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        // 현재 사용자의 알림인지 확인
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_OWNED_BY_USER);
        }

        // 알림 타입 확인
        if (notification.getType() != expectedType) {
            throw new NotificationException(NotificationErrorCode.INVALID_NOTIFICATION_TYPE);
        }

        // 관련 엔티티 ID가 있는지 확인
        if (notification.getRelatedEntityId() == null) {
            throw new NotificationException(NotificationErrorCode.RELATED_ENTITY_NOT_FOUND);
        }

        return notification;
    }
}
