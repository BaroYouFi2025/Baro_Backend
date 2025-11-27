package baro.baro.domain.notification.service;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.user.entity.User;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissingPersonNotificationServiceTest {

    @Mock
    private NotificationDeviceService notificationDeviceService;
    @Mock
    private FcmDispatchService fcmDispatchService;
    @Mock
    private NotificationPersistenceService notificationPersistenceService;

    private MissingPersonNotificationService missingPersonNotificationService;

    @BeforeEach
    void setUp() {
        missingPersonNotificationService = new MissingPersonNotificationService(
                notificationDeviceService,
                fcmDispatchService,
                notificationPersistenceService
        );
    }

    @Test
    void sendMissingPersonFoundNotificationDispatchesForEachDevice() {
        User owner = createUser(1L, "등록자");
        Device first = Device.builder().user(owner).fcmToken("token-1").isActive(true).build();
        Device second = Device.builder().user(owner).fcmToken("token-2").isActive(true).build();
        when(notificationDeviceService.getActiveDevicesWithToken(owner))
                .thenReturn(List.of(first, second));
        Message message1 = Message.builder().setToken("token-1").build();
        Message message2 = Message.builder().setToken("token-2").build();
        when(fcmDispatchService.buildMissingPersonFoundMessage(
                eq("token-1"), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(message1);
        when(fcmDispatchService.buildMissingPersonFoundMessage(
                eq("token-2"), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(message2);

        String missingPersonName = "실종자";
        String reporterName = "제보자";
        String address = "서울시";
        String expectedMessage = String.format("실종자 %s님이 발견되었습니다\n\n찾은 팀: %s 님\n발견 위치: %s",
                missingPersonName, reporterName, address);

        missingPersonNotificationService.sendMissingPersonFoundNotification(
                10L, owner, missingPersonName, reporterName, address
        );

        verify(notificationPersistenceService).save(owner, NotificationType.FOUND_REPORT,
                "실종자가 발견되었습니다!", expectedMessage, 10L);
        verify(fcmDispatchService).dispatch(message1, "missing_person_found", "token-1");
        verify(fcmDispatchService).dispatch(message2, "missing_person_found", "token-2");
    }

    @Test
    void sendMissingPersonFoundNotificationSkipsDispatchWhenNoDevices() {
        User owner = createUser(2L, "등록자");
        when(notificationDeviceService.getActiveDevicesWithToken(owner)).thenReturn(List.of());

        missingPersonNotificationService.sendMissingPersonFoundNotification(
                11L, owner, "실종자", "제보자", "서울"
        );

        verify(notificationPersistenceService).save(eq(owner), eq(NotificationType.FOUND_REPORT),
                anyString(), anyString(), eq(11L));
        verify(fcmDispatchService, never()).dispatch(any(), anyString(), anyString());
    }

    @Test
    void sendNearbyAlertToReporterPersistsLocationAndDispatches() {
        User reporter = createUser(3L, "제보자");
        Device device = Device.builder().user(reporter).fcmToken("token-3").isActive(true).build();
        when(notificationDeviceService.getActiveDevicesWithToken(reporter))
                .thenReturn(List.of(device));
        Message message = Message.builder().setToken("token-3").build();
        when(fcmDispatchService.buildNearbyAlertMessage(
                eq("token-3"), anyString(), anyString(), anyString(), eq("제보자"),
                anyDouble(), anyLong(), eq("reporter")
        )).thenReturn(message);
        Point location = new GeometryFactory().createPoint(new Coordinate(127.1, 37.5));

        double distance = 50.0;
        String missingPersonName = "실종자";
        String expectedMessage = String.format("실종자 %s가 주변 %.0fm 이내에 있습니다. 주의 깊게 살펴봐 주세요.",
                missingPersonName, distance);

        missingPersonNotificationService.sendNearbyAlertToReporter(
                reporter, missingPersonName, distance, location, 55L
        );

        verify(notificationPersistenceService).saveWithLocation(reporter, NotificationType.NEARBY_ALERT,
                "주변에 실종자가 있습니다!", expectedMessage,
                55L, location);
        verify(fcmDispatchService).dispatch(message, "nearby_alert", "token-3");
    }

    private User createUser(Long id, String name) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "name", name);
        return user;
    }
}
