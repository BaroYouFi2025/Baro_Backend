package baro.baro.domain.member.service;

import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.member.dto.request.AcceptInvitationRequest;
import baro.baro.domain.member.dto.request.InvitationRequest;
import baro.baro.domain.member.dto.request.RejectInvitationRequest;
import baro.baro.domain.member.dto.response.AcceptInvitationResponse;
import baro.baro.domain.member.dto.response.InvitationResponse;
import baro.baro.domain.member.entity.Invitation;
import baro.baro.domain.member.entity.Relationship;
import baro.baro.domain.member.entity.RelationshipRequestStatus;
import baro.baro.domain.member.exception.MemberErrorCode;
import baro.baro.domain.member.exception.MemberException;
import baro.baro.domain.member.repository.InvitationRepository;
import baro.baro.domain.member.repository.RelationshipRepository;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.device.repository.GpsTrackRepository;
import baro.baro.domain.notification.service.PushNotificationService;
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

import java.time.LocalDate;
import java.util.Optional;

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
    private PushNotificationService pushNotificationService;

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

        invitee = User.builder()
                .uid("invitee_uid")
                .name("피초대자")
                .phoneE164("+821087654321")
                .birthDate(LocalDate.of(1995, 5, 5))
                .encodedPassword("hashedPassword")
                .build();

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
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(1L);
        request.setRelation("친구");

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
        }
    }

    @Test
    @DisplayName("초대 수락 실패 - 초대를 찾을 수 없음")
    void acceptInvitation_InvitationNotFound() {
        // given
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setRelationshipRequestId(999L);

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
}
