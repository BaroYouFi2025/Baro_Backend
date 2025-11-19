package baro.baro.domain.notification.service;

import baro.baro.domain.member.dto.res.AcceptInvitationResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.notification.dto.res.NotificationResponse;
import baro.baro.domain.missingperson.dto.res.SightingDetailResponse;

import java.util.List;

// 알림 서비스 인터페이스
public interface NotificationServiceInterface {

    // 현재 로그인한 사용자의 모든 알림 조회
    List<NotificationResponse> getMyNotifications();

    // 현재 로그인한 사용자의 읽지 않은 알림만 조회
    List<NotificationResponse> getUnreadNotifications();

    // 현재 로그인한 사용자의 읽지 않은 알림 개수 조회
    long getUnreadCount();

    // 알림 읽음 처리
    void markAsRead(Long notificationId);

    // 알림을 통한 초대 수락
    AcceptInvitationResponse acceptInvitationFromNotification(Long notificationId, String relation);

    // 알림을 통한 초대 거절
    void rejectInvitationFromNotification(Long notificationId);

    // 알림을 통한 실종자 상세 조회
    MissingPersonDetailResponse getMissingPersonDetailFromNotification(Long notificationId);

    // 알림을 통한 발견 신고 상세 조회
    SightingDetailResponse getSightingDetailFromNotification(Long notificationId);
}
