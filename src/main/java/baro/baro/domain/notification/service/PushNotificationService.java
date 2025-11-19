package baro.baro.domain.notification.service;

import baro.baro.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 푸시 알림 서비스 Facade
// 기존 호출 코드와의 호환성을 유지하면서
// 실제 구현을 전문화된 서비스들에 위임합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final InvitationNotificationService invitationNotificationService;
    private final MissingPersonNotificationService missingPersonNotificationService;

    // 초대 요청 푸시 알림을 발송합니다.
    //
    // @param invitee 초대받은 사용자
    // @param inviter 초대한 사용자
    // @param relation 관계
    // @param invitationId 초대 ID
    @Transactional
    public void sendInvitationNotification(User invitee, User inviter, String relation, Long invitationId) {
        invitationNotificationService.sendInvitationNotification(invitee, inviter, relation, invitationId);
    }

    // 초대 응답 푸시 알림을 발송합니다.
    //
    // @param inviter 초대한 사용자
    // @param invitee 초대받은 사용자
    // @param isAccepted 수락 여부
    // @param relation 관계
    @Transactional
    public void sendInvitationResponseNotification(User inviter, User invitee, boolean isAccepted, String relation) {
        invitationNotificationService.sendInvitationResponseNotification(inviter, invitee, isAccepted, relation);
    }

    // 실종자 발견 신고 푸시 알림을 발송합니다.
    //
    // @param sightingId 목격 ID
    // @param missingPersonOwner 실종자 등록자
    // @param missingPersonName 실종자 이름
    // @param reporterName 신고자 이름
    // @param address 발견 위치 주소
    @Transactional
    public void sendMissingPersonFoundNotification(
            Long sightingId,
            User missingPersonOwner,
            String missingPersonName,
            String reporterName,
            String address) {
        missingPersonNotificationService.sendMissingPersonFoundNotification(
                sightingId, missingPersonOwner, missingPersonName, reporterName, address);
    }

    // GPS 업데이트한 사용자에게 NEARBY_ALERT 푸시 알림을 발송합니다.
    //
    // @param reporter GPS 업데이트한 사용자
    // @param missingPersonName 실종자 이름
    // @param distance 거리 (미터)
    // @param reporterLocation 사용자 현재 위치
    // @param missingPersonId 실종자 ID
    @Transactional
    public void sendNearbyAlertToReporter(User reporter, String missingPersonName,
                                           double distance, Point reporterLocation,
                                           Long missingPersonId) {
        missingPersonNotificationService.sendNearbyAlertToReporter(
                reporter, missingPersonName, distance, reporterLocation, missingPersonId);
    }
}
