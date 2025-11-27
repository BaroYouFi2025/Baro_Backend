package baro.baro.domain.member.service;

import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.entity.GpsTrack;
import baro.baro.domain.device.exception.DeviceErrorCode;
import baro.baro.domain.device.exception.DeviceException;
import baro.baro.domain.member.dto.req.AcceptInvitationRequest;
import baro.baro.domain.member.dto.req.InvitationRequest;
import baro.baro.domain.member.dto.req.RejectInvitationRequest;
import baro.baro.domain.member.dto.res.AcceptInvitationResponse;
import baro.baro.domain.member.dto.res.InvitationResponse;
import baro.baro.domain.member.dto.res.MemberLocationResponse;
import baro.baro.domain.member.entity.Invitation;
import baro.baro.domain.member.entity.Relationship;
import baro.baro.domain.member.entity.RelationshipRequestStatus;
import baro.baro.domain.member.exception.MemberErrorCode;
import baro.baro.domain.member.exception.MemberException;
import baro.baro.domain.member.repository.InvitationRepository;
import baro.baro.domain.member.repository.RelationshipRepository;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.device.repository.GpsTrackRepository;
import baro.baro.domain.member.dto.event.InvitationCreatedEvent;
import baro.baro.domain.member.dto.event.InvitationResponseEvent;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 테스트")
class MemberServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RelationshipRepository relationshipRepository;

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private GpsTrackRepository gpsTrackRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MemberServiceImpl memberService;

    private User inviter;
    private User invitee;
    private Invitation invitation;

    @BeforeEach
    void setUp() {
        inviter = User.builder()
                .uid("inviter_uid")
                .name("초대자")
                .phoneE164("+821012345678")
                .birthDate(LocalDate.of(1990, 1, 1))
                .encodedPassword("hashedPassword")
                .build();
        ReflectionTestUtils.setField(inviter, "id", 1L);

        invitee = User.builder()
                .uid("invitee_uid")
                .name("피초대자")
                .phoneE164("+821087654321")
                .birthDate(LocalDate.of(1995, 5, 5))
                .encodedPassword("hashedPassword")
                .build();
        ReflectionTestUtils.setField(invitee, "id", 2L);

        invitation = Invitation.builder()
                .id(1L)
                .inviterUser(inviter)
                .inviteeUser(invitee)
                .relation("가족")
                .status(RelationshipRequestStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("초대 생성 성공")
    void makeInvitation_Success() {
        // given
        InvitationRequest request = new InvitationRequest();
        request.setInviteeUserId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(invitee));
        when(invitationRepository.save(any(Invitation.class))).thenReturn(invitation);

        // when
        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(inviter);

            InvitationResponse response = memberService.makeInvitation(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRelationshipRequestId()).isEqualTo(1L);
            verify(userRepository, times(1)).findById(2L);
            verify(invitationRepository, times(1)).save(any(Invitation.class));
            verify(eventPublisher).publishEvent(any(InvitationCreatedEvent.class));
        }
    }

    @Test
    @DisplayName("초대 생성 실패 - 피초대자를 찾을 수 없음")
    void makeInvitation_UserNotFound() {
        // given
        InvitationRequest request = new InvitationRequest();
        request.setInviteeUserId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(inviter);

            assertThatThrownBy(() -> memberService.makeInvitation(request))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("userErrorCode", UserErrorCode.USER_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("초대 수락 성공")
    void acceptInvitation_Success() {
        // given
        AcceptInvitationRequest request = new AcceptInvitationRequest(1L, "친구");

        Relationship originRelationship = Relationship.builder()
                .id(1L)
                .user(inviter)
                .member(invitee)
                .relation("가족")
                .build();

        Relationship reverseRelationship = Relationship.builder()
                .id(2L)
                .user(invitee)
                .member(inviter)
                .relation("친구")
                .build();

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenReturn(invitation);
        when(relationshipRepository.save(any(Relationship.class)))
                .thenReturn(originRelationship)
                .thenReturn(reverseRelationship);

        // when
        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(invitee);

            AcceptInvitationResponse response = memberService.acceptInvitation(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getRelationshipIds()).hasSize(2);
            verify(invitationRepository, times(1)).findById(1L);
            verify(invitationRepository, times(1)).save(any(Invitation.class));
            verify(relationshipRepository, times(2)).save(any(Relationship.class));
            verify(eventPublisher).publishEvent(any(InvitationResponseEvent.class));
        }
    }

    @Test
    @DisplayName("초대 수락 실패 - 초대를 찾을 수 없음")
    void acceptInvitation_InvitationNotFound() {
        // given
        AcceptInvitationRequest request = new AcceptInvitationRequest(999L, null);

        when(invitationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(invitee);

            assertThatThrownBy(() -> memberService.acceptInvitation(request))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("memberErrorCode", MemberErrorCode.INVITATION_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("초대 거절 성공")
    void rejectInvitation_Success() {
        // given
        RejectInvitationRequest request = new RejectInvitationRequest();
        request.setRelationshipId(1L);

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(Invitation.class))).thenReturn(invitation);

        // when
        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(invitee);

            memberService.rejectInvitation(request);

            // then
            verify(invitationRepository, times(1)).findById(1L);
            verify(invitationRepository, times(1)).save(any(Invitation.class));
            verify(eventPublisher).publishEvent(any(InvitationResponseEvent.class));
        }
    }

    @Test
    @DisplayName("초대 거절 실패 - 초대를 찾을 수 없음")
    void rejectInvitation_InvitationNotFound() {
        // given
        RejectInvitationRequest request = new RejectInvitationRequest();
        request.setRelationshipId(999L);

        when(invitationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        try (MockedStatic<SecurityUtil> securityUtil =
                     mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(invitee);

            assertThatThrownBy(() -> memberService.rejectInvitation(request))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("memberErrorCode", MemberErrorCode.INVITATION_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("구성원 위치 조회 - 현재 사용자 기준")
    void getMemberLocations_returnsLocationResponses() {
        Relationship relationship = Relationship.builder()
                .user(inviter)
                .member(invitee)
                .relation("가족")
                .build();

        Device inviterDevice = deviceWithBattery(inviter, 85);
        Device inviteeDevice = deviceWithBattery(invitee, 42);
        GpsTrack inviterTrack = gpsTrack(inviterDevice, 37.5665, 126.9780);
        GpsTrack inviteeTrack = gpsTrack(inviteeDevice, 37.5700, 126.9820);

        when(deviceRepository.findByUser(inviter)).thenReturn(List.of(inviterDevice));
        when(deviceRepository.findByUser(invitee)).thenReturn(List.of(inviteeDevice));
        when(gpsTrackRepository.findLatestByDevice(inviterDevice)).thenReturn(Optional.of(inviterTrack));
        when(gpsTrackRepository.findLatestByDevice(inviteeDevice)).thenReturn(Optional.of(inviteeTrack));
        when(relationshipRepository.findByUserWithMember(inviter)).thenReturn(List.of(relationship));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(inviter);

            List<MemberLocationResponse> responses = memberService.getMemberLocations();

            assertThat(responses).hasSize(1);
            MemberLocationResponse response = responses.get(0);
            assertThat(response.getUserId()).isEqualTo(invitee.getId());
            assertThat(response.getRelationship()).isEqualTo("가족");
            assertThat(response.getBatteryLevel()).isEqualTo(42);
            assertThat(response.getLocation().getLatitude()).isEqualTo(37.57);
            assertThat(response.getLocation().getLongitude()).isEqualTo(126.982);
            assertThat(response.getDistance()).isGreaterThan(0.0);
        }
    }

    @Test
    @DisplayName("구성원 위치 조회 - 기기나 위치 없는 구성원은 제외")
    void getMemberLocations_skipsMembersWithoutDeviceOrLocation() {
        User memberWithoutDevice = cloneUser(3L, "디바이스없음");
        User memberWithoutLocation = cloneUser(4L, "위치없음");
        User memberWithLocation = cloneUser(5L, "정상");

        Relationship rel1 = Relationship.builder().user(inviter).member(memberWithoutDevice).relation("친구").build();
        Relationship rel2 = Relationship.builder().user(inviter).member(memberWithoutLocation).relation("동료").build();
        Relationship rel3 = Relationship.builder().user(inviter).member(memberWithLocation).relation("가족").build();

        Device inviterDevice = deviceWithBattery(inviter, 55);
        Device memberDeviceNoLocation = deviceWithBattery(memberWithoutLocation, 60);
        Device memberDeviceWithLocation = deviceWithBattery(memberWithLocation, 30);

        when(deviceRepository.findByUser(inviter)).thenReturn(List.of(inviterDevice));
        when(gpsTrackRepository.findLatestByDevice(inviterDevice)).thenReturn(Optional.empty());

        when(deviceRepository.findByUser(memberWithoutDevice)).thenReturn(Collections.emptyList());

        when(deviceRepository.findByUser(memberWithoutLocation)).thenReturn(List.of(memberDeviceNoLocation));
        when(gpsTrackRepository.findLatestByDevice(memberDeviceNoLocation)).thenReturn(Optional.empty());

        when(deviceRepository.findByUser(memberWithLocation)).thenReturn(List.of(memberDeviceWithLocation));
        when(gpsTrackRepository.findLatestByDevice(memberDeviceWithLocation))
                .thenReturn(Optional.of(gpsTrack(memberDeviceWithLocation, 35.0, 129.0)));

        when(relationshipRepository.findByUserWithMember(inviter)).thenReturn(List.of(rel1, rel2, rel3));

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(inviter);

            List<MemberLocationResponse> responses = memberService.getMemberLocations();

            assertThat(responses).hasSize(1);
            MemberLocationResponse response = responses.get(0);
            assertThat(response.getUserId()).isEqualTo(memberWithLocation.getId());
            assertThat(response.getDistance()).isEqualTo(0.0);
        }
    }

    @Test
    @DisplayName("특정 사용자 기준 구성원 위치 조회 - 성공")
    void getMemberLocationsForUser_returnsResponses() {
        Relationship relationship = Relationship.builder()
                .user(inviter)
                .member(invitee)
                .relation("가족")
                .build();

        Device inviterDevice = deviceWithBattery(inviter, 80);
        Device inviteeDevice = deviceWithBattery(invitee, 30);
        GpsTrack inviterTrack = gpsTrack(inviterDevice, 37.5, 127.0);
        GpsTrack inviteeTrack = gpsTrack(inviteeDevice, 37.7, 127.1);

        when(userRepository.findById(inviter.getId())).thenReturn(Optional.of(inviter));
        when(deviceRepository.findByUser(inviter)).thenReturn(List.of(inviterDevice));
        when(deviceRepository.findByUser(invitee)).thenReturn(List.of(inviteeDevice));
        when(gpsTrackRepository.findLatestByDevice(inviterDevice)).thenReturn(Optional.of(inviterTrack));
        when(gpsTrackRepository.findLatestByDevice(inviteeDevice)).thenReturn(Optional.of(inviteeTrack));
        when(relationshipRepository.findByUserWithMember(inviter)).thenReturn(List.of(relationship));

        List<MemberLocationResponse> responses = memberService.getMemberLocationsForUser(inviter.getId());

        assertThat(responses).hasSize(1);
        MemberLocationResponse response = responses.get(0);
        assertThat(response.getUserId()).isEqualTo(invitee.getId());
        assertThat(response.getBatteryLevel()).isEqualTo(30);
        verify(userRepository).findById(inviter.getId());
    }

    @Test
    @DisplayName("특정 사용자 기준 구성원 위치 조회 - 사용자를 찾지 못하면 예외")
    void getMemberLocationsForUser_userNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMemberLocationsForUser(99L))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("userErrorCode", UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("구성원 위치 조회 - 사용자 기기가 없으면 예외")
    void getMemberLocations_userWithoutDeviceThrowsException() {
        when(deviceRepository.findByUser(inviter)).thenReturn(Collections.emptyList());

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(inviter);

            assertThatThrownBy(() -> memberService.getMemberLocations())
                    .isInstanceOf(DeviceException.class)
                    .hasFieldOrPropertyWithValue("deviceErrorCode", DeviceErrorCode.DEVICE_NOT_FOUND);
        }
    }

    private Device deviceWithBattery(User owner, int battery) {
        return Device.builder()
                .id(10L)
                .user(owner)
                .deviceUuid("uuid-" + owner.getUid() + "-" + battery)
                .batteryLevel(battery)
                .isActive(true)
                .build();
    }

    private GpsTrack gpsTrack(Device device, double latitude, double longitude) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return GpsTrack.builder()
                .device(device)
                .location(point)
                .recordedAt(LocalDateTime.now())
                .build();
    }

    private User cloneUser(long id, String name) {
        User user = User.builder()
                .uid("user-" + id)
                .name(name)
                .phoneE164("+8210" + id)
                .birthDate(LocalDate.of(1990, 1, 1))
                .encodedPassword("pw")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
