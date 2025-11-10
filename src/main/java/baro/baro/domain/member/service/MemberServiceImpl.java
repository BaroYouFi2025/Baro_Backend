package baro.baro.domain.member.service;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.entity.GpsTrack;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.device.repository.GpsTrackRepository;
import baro.baro.domain.common.util.GpsUtils;
import baro.baro.domain.notification.service.PushNotificationService;
import baro.baro.domain.member.dto.request.AcceptInvitationRequest;
import baro.baro.domain.member.dto.request.InvitationRequest;
import baro.baro.domain.member.dto.request.RejectInvitationRequest;
import baro.baro.domain.member.dto.response.AcceptInvitationResponse;
import baro.baro.domain.member.dto.response.InvitationResponse;
import baro.baro.domain.member.dto.response.MemberLocationResponse;
import baro.baro.domain.member.entity.Invitation;
import baro.baro.domain.member.entity.Relationship;
import baro.baro.domain.member.entity.RelationshipRequestStatus;
import baro.baro.domain.member.exception.MemberErrorCode;
import baro.baro.domain.member.exception.MemberException;
import baro.baro.domain.member.repository.InvitationRepository;
import baro.baro.domain.member.repository.RelationshipRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static baro.baro.domain.common.util.SecurityUtil.getCurrentUser;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;
    private final InvitationRepository invitationRepository;
    private final DeviceRepository deviceRepository;
    private final GpsTrackRepository gpsTrackRepository;
    private final PushNotificationService pushNotificationService;

    @Override
    @Transactional // 구성원 초대 생성 메서드
    public InvitationResponse makeInvitation(InvitationRequest request) {
        User currentUser = getCurrentUser();
        User invitee = userRepository.findById(request.getInviteeUserId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Invitation invitationRequest = Invitation.builder()
                .inviterUser(currentUser)
                .inviteeUser(invitee)
                .relation(request.getRelation())
                .status(RelationshipRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Invitation created = invitationRepository.save(invitationRequest);
        
        // 푸시 알림 발송
        pushNotificationService.sendInvitationNotification(invitee, currentUser, request.getRelation());
        
        return new InvitationResponse(created.getId());
    }

    @Override
    @Transactional // 구성원 초대 동의 메서드
    public AcceptInvitationResponse acceptInvitation(AcceptInvitationRequest request) {
        User currentUser = getCurrentUser();

        Invitation invitation = invitationRepository.findById(request.getRelationshipRequestId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.INVITATION_NOT_FOUND));

        // 초대된 사용자가 맞는지 확인
        invitation.validateInvitee(currentUser);

        // 초대 상태가 PENDING인지 확인(중복 수락 방지)
        // status가 PENDING이 아니면 예외 발생
        invitation.accept();
        invitationRepository.save(invitation);

        User inviter = invitation.getInviterUser();

        // 양방향 관계를 위해 두 개의 Relationship 엔티티 생성
        Relationship originRelationship = Relationship.builder()
                .user(inviter)
                .member(currentUser)
                .relation(invitation.getRelation())
                .createdAt(LocalDateTime.now())
                .build();

        Relationship reverseRelationship = Relationship.builder()
                .user(currentUser)
                .member(inviter)
                .relation(request.getRelation())
                .createdAt(LocalDateTime.now())
                .build();


        originRelationship = relationshipRepository.save(originRelationship);
        reverseRelationship = relationshipRepository.save(reverseRelationship);
        
        // 초대 수락 푸시 알림 발송
        pushNotificationService.sendInvitationResponseNotification(inviter, currentUser, true, request.getRelation());
        
        return AcceptInvitationResponse.of(originRelationship.getId(), reverseRelationship.getId());
    }

    @Override
    @Transactional // 구성원 초대 거절 메서드
    public void rejectInvitation(RejectInvitationRequest request) {
        User currentUser = getCurrentUser();

        Invitation invitation = invitationRepository.findById(request.getRelationshipId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.INVITATION_NOT_FOUND));

        // 초대된 사용자가 맞는지 확인
        invitation.validateInvitee(currentUser);

        // 초대 상태가 PENDING인지 확인(중복 거절 방지)
        invitation.reject();
        invitationRepository.save(invitation);
        
        // 초대 거절 푸시 알림 발송
        User inviter = invitation.getInviterUser();
        pushNotificationService.sendInvitationResponseNotification(inviter, currentUser, false, invitation.getRelation());
    }


    /**
     * 사용자와 관계가 있는 구성원들의 위치 정보를 조회합니다.
     * 각 구성원의 이름, 관계, 최신 GPS 위치, 배터리 레벨, 사용자와의 거리를 반환합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MemberLocationResponse> getMemberLocations() {
        User currentUser = getCurrentUser();

        // 현재 사용자의 최신 위치 조회 (거리 계산용)
        Device currentUserDevice = deviceRepository.findByUser(currentUser).stream()
                .findFirst()
                .orElse(null);

        GpsTrack currentUserLocation = null;
        if (currentUserDevice != null) {
            currentUserLocation = gpsTrackRepository.findLatestByDevice(currentUserDevice)
                    .orElse(null);
        }

        // 관계 목록 조회 (N+1 방지 fetch join)
        List<Relationship> relationships = relationshipRepository.findByUserWithMember(currentUser);

        List<MemberLocationResponse> members = new ArrayList<>();
        for (Relationship relationship : relationships) {
            User member = relationship.getMember();

            // 구성원의 기기 조회
            Device memberDevice = deviceRepository.findByUser(member).stream()
                    .findFirst()
                    .orElse(null);

            if (memberDevice == null) {
                continue;
            }

            // 구성원의 최신 GPS 위치 조회
            GpsTrack memberLocation = gpsTrackRepository.findLatestByDevice(memberDevice)
                    .orElse(null);

            if (memberLocation == null) {
                continue;
            }

            // 거리 계산
            Double distance = 0.0;
            if (currentUserLocation != null && currentUserLocation.getLocation() != null) {
                distance = GpsUtils.calculateDistance(
                        currentUserLocation.getLocation(),
                        memberLocation.getLocation()
                );
            }

            // 위치 정보 DTO
            MemberLocationResponse.LocationInfo location = MemberLocationResponse.LocationInfo.create(
                    GpsUtils.getLatitude(memberLocation.getLocation()),
                    GpsUtils.getLongitude(memberLocation.getLocation())
            );

            // 구성원 응답 DTO
            MemberLocationResponse memberResponse = MemberLocationResponse.create(
                    member.getId(),
                    member.getName(),
                    relationship.getRelation(),
                    memberDevice.getBatteryLevel(),
                    distance,
                    location
            );

            members.add(memberResponse);
        }

        return members;
    }
}
