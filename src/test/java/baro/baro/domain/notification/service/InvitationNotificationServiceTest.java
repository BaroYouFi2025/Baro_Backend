package baro.baro.domain.notification.service;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.user.entity.User;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationNotificationServiceTest {

    @Mock
    private NotificationDeviceService notificationDeviceService;
    @Mock
    private FcmDispatchService fcmDispatchService;
    @Mock
    private NotificationPersistenceService notificationPersistenceService;

    private InvitationNotificationService invitationNotificationService;

    @BeforeEach
    void setUp() {
        invitationNotificationService = new InvitationNotificationService(
                notificationDeviceService,
                fcmDispatchService,
                notificationPersistenceService
        );
    }

    @Test
    void sendInvitationNotificationDispatchesForEachDevice() {
        User invitee = createUser(1L, "수신자");
        User inviter = createUser(2L, "초대한 사람");
        Device device = Device.builder()
                .user(invitee)
                .fcmToken("token-123")
                .isActive(true)
                .build();
        when(notificationDeviceService.getActiveDevicesWithToken(invitee))
                .thenReturn(List.of(device));
        String relation = "아버지";
        Long invitationId = 77L;
        String expectedMessage = String.format("%s님이 %s로 초대 요청을 보냈습니다.",
                inviter.getName(), relation);
        Message fcmMessage = Message.builder().setToken("token-123").build();
        when(fcmDispatchService.buildInvitationMessage("token-123", "새로운 구성원 초대 요청",
                expectedMessage, invitationId, inviter.getName(), relation)).thenReturn(fcmMessage);

        invitationNotificationService.sendInvitationNotification(invitee, inviter, relation, invitationId);

        verify(notificationPersistenceService).save(invitee, NotificationType.INVITE_REQUEST,
                "새로운 구성원 초대 요청", expectedMessage, invitationId);
        verify(fcmDispatchService).dispatch(fcmMessage, "invitation", "token-123");
    }

    @Test
    void sendInvitationNotificationSkipsDispatchWhenNoDevices() {
        User invitee = createUser(3L, "기기 없음");
        User inviter = createUser(4L, "초대자");
        when(notificationDeviceService.getActiveDevicesWithToken(invitee))
                .thenReturn(List.of());

        invitationNotificationService.sendInvitationNotification(invitee, inviter, "자녀", 1L);

        verify(notificationPersistenceService).save(eq(invitee), eq(NotificationType.INVITE_REQUEST),
                anyString(), anyString(), eq(1L));
        verify(fcmDispatchService, never()).dispatch(any(), anyString(), anyString());
    }

    @Test
    void sendInvitationResponseNotificationBuildsMessageByAcceptance() {
        User inviter = createUser(5L, "초대한 사용자");
        User invitee = createUser(6L, "수락한 사용자");
        Device device = Device.builder()
                .user(inviter)
                .fcmToken("token-77")
                .isActive(true)
                .build();
        when(notificationDeviceService.getActiveDevicesWithToken(inviter))
                .thenReturn(List.of(device));
        Message fcmMessage = Message.builder().setToken("token-77").build();
        String relation = "딸";
        String expectedMessage = String.format("%s님이 %s 초대 요청을 수락했습니다.",
                invitee.getName(), relation);
        when(fcmDispatchService.buildInvitationResponseMessage(
                "token-77",
                "초대 요청이 수락되었습니다",
                expectedMessage,
                "invitation_accepted",
                invitee.getName(),
                relation,
                true
        )).thenReturn(fcmMessage);

        invitationNotificationService.sendInvitationResponseNotification(inviter, invitee, true, relation);

        verify(notificationPersistenceService).save(inviter, NotificationType.INVITE_REQUEST,
                "초대 요청이 수락되었습니다", expectedMessage, null);
        verify(fcmDispatchService).dispatch(fcmMessage, "invitation_accepted", "token-77");
    }

    private User createUser(Long id, String name) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "name", name);
        return user;
    }
}
