package baro.baro.domain.device.service;

import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.device.dto.req.DeviceRegisterRequest;
import baro.baro.domain.device.dto.req.FcmTokenUpdateRequest;
import baro.baro.domain.device.dto.req.GpsUpdateRequest;
import baro.baro.domain.device.dto.res.DeviceResponse;
import baro.baro.domain.device.dto.res.GpsUpdateResponse;
import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.entity.GpsTrack;
import baro.baro.domain.device.exception.DeviceException;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.device.repository.GpsTrackRepository;
import baro.baro.domain.member.dto.event.MemberLocationChangedEvent;
import baro.baro.domain.missingperson.entity.CaseStatusType;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.repository.MissingCaseRepository;
import baro.baro.domain.missingperson.repository.MissingPersonRepository;
import baro.baro.domain.notification.entity.Notification;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.notification.repository.NotificationRepository;
import baro.baro.domain.notification.service.PushNotificationService;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GpsTrackRepository gpsTrackRepository;

    @Mock
    private MissingPersonRepository missingPersonRepository;

    @Mock
    private MissingCaseRepository missingCaseRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private MetricsService metricsService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DeviceServiceImpl deviceService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        deviceService = new DeviceServiceImpl(
                deviceRepository,
                userRepository,
                gpsTrackRepository,
                missingPersonRepository,
                missingCaseRepository,
                notificationRepository,
                pushNotificationService,
                metricsService,
                eventPublisher
        );
        ReflectionTestUtils.setField(deviceService, "nearbyAlertRadiusMeters", 1000);
        ReflectionTestUtils.setField(deviceService, "nearbyAlertCooldownHours", 24);
        ReflectionTestUtils.setField(deviceService, "nearbyAlertDistanceThresholdMeters", 500d);
    }

    @Test
    void registerDevicePersistsNewDeviceForCurrentUser() {
        DeviceRegisterRequest request = createRegisterRequest();
        User currentUser = createUser(1L, "Reporter");

        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            when(deviceRepository.findByDeviceUuid(request.getDeviceUuid()))
                    .thenReturn(Optional.empty());
            when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> {
                Device device = invocation.getArgument(0);
                ReflectionTestUtils.setField(device, "id", 99L);
                return device;
            });

            DeviceResponse response = deviceService.registerDevice(request);

            assertThat(response.getDeviceId()).isEqualTo(99L);
            assertThat(response.getDeviceUuid()).isEqualTo(request.getDeviceUuid());
            verify(deviceRepository).save(any(Device.class));
        }
    }

    @Test
    void registerDeviceThrowsWhenUuidExists() {
        DeviceRegisterRequest request = createRegisterRequest();
        User currentUser = createUser(2L, "Owner");

        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            when(deviceRepository.findByDeviceUuid(request.getDeviceUuid()))
                    .thenReturn(Optional.of(createDevice(10L, currentUser)));

            assertThrows(DeviceException.class, () -> deviceService.registerDevice(request));
            verify(deviceRepository, never()).save(any(Device.class));
        }
    }

    @Test
    void updateGpsSavesTrackPublishesEventsAndMetrics() {
        GpsUpdateRequest request = createGpsRequest(37.5, 127.0, 75);
        User currentUser = createUser(11L, "Tracker");
        Device device = createDevice(20L, currentUser);

        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            mockedStatic.when(SecurityUtil::getCurrentDeviceId).thenReturn(device.getId());

            when(deviceRepository.findByIdAndUser(device.getId(), currentUser))
                    .thenReturn(Optional.of(device));
            when(missingPersonRepository.findNearbyMissingPersons(anyDouble(), anyDouble(), anyInt()))
                    .thenReturn(List.of());
            when(gpsTrackRepository.save(any(GpsTrack.class))).thenAnswer(invocation -> invocation.getArgument(0));

            GpsUpdateResponse response = deviceService.updateGps(request);

            assertThat(response.getLatitude()).isEqualTo(request.getLatitude());
            assertThat(response.getLongitude()).isEqualTo(request.getLongitude());
            assertThat(device.getBatteryLevel()).isEqualTo(request.getBatteryLevel());

            ArgumentCaptor<GpsTrack> trackCaptor = ArgumentCaptor.forClass(GpsTrack.class);
            verify(gpsTrackRepository).save(trackCaptor.capture());
            Point savedPoint = trackCaptor.getValue().getLocation();
            assertThat(savedPoint.getX()).isEqualTo(request.getLongitude());
            assertThat(savedPoint.getY()).isEqualTo(request.getLatitude());

            ArgumentCaptor<MemberLocationChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(MemberLocationChangedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getUserId()).isEqualTo(currentUser.getId());

            verify(metricsService).recordGpsLocationUpdate();
            verify(metricsService).recordGpsUpdateDuration(anyLong());
            verify(missingPersonRepository)
                    .findNearbyMissingPersons(request.getLatitude(), request.getLongitude(), 1000);
            verify(pushNotificationService, never()).sendNearbyAlertToReporter(
                    any(User.class), any(), anyDouble(), any(Point.class), anyLong());
        }
    }

    @Test
    void updateGpsThrowsWhenDeviceIdMissing() {
        GpsUpdateRequest request = createGpsRequest(36.0, 128.0, null);
        User currentUser = createUser(30L, "Tracker");

        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            mockedStatic.when(SecurityUtil::getCurrentDeviceId).thenReturn(null);

            assertThrows(DeviceException.class, () -> deviceService.updateGps(request));
        }
    }

    @Test
    void updateGpsThrowsWhenDeviceNotOwnedByUser() {
        GpsUpdateRequest request = createGpsRequest(36.0, 128.0, null);
        User currentUser = createUser(31L, "Tracker");

        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            mockedStatic.when(SecurityUtil::getCurrentDeviceId).thenReturn(777L);

            when(deviceRepository.findByIdAndUser(777L, currentUser))
                    .thenReturn(Optional.empty());

            assertThrows(DeviceException.class, () -> deviceService.updateGps(request));
        }
    }

    @Test
    void updateFcmTokenUpdatesFirstActiveDevice() {
        User user = createUser(40L, "Owner");
        FcmTokenUpdateRequest request = new FcmTokenUpdateRequest();
        request.setFcmToken("new-token");
        Device activeDevice = createDevice(200L, user);

        when(userRepository.findByUid("owner-uid")).thenReturn(Optional.of(user));
        when(deviceRepository.findByUser(user)).thenReturn(List.of(activeDevice));
        when(deviceRepository.save(activeDevice)).thenReturn(activeDevice);

        deviceService.updateFcmToken("owner-uid", request);

        assertThat(activeDevice.getFcmToken()).isEqualTo("new-token");
        verify(deviceRepository).save(activeDevice);
    }

    @Test
    void updateFcmTokenThrowsWhenUserMissing() {
        FcmTokenUpdateRequest request = new FcmTokenUpdateRequest();
        request.setFcmToken("token");
        when(userRepository.findByUid("unknown")).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> deviceService.updateFcmToken("unknown", request));
    }

    @Test
    void updateFcmTokenThrowsWhenNoActiveDevice() {
        User user = createUser(41L, "Owner");
        FcmTokenUpdateRequest request = new FcmTokenUpdateRequest();
        request.setFcmToken("token");

        when(userRepository.findByUid("owner")).thenReturn(Optional.of(user));
        Device inactiveDevice = Device.builder()
                .id(1L)
                .user(user)
                .deviceUuid("inactive")
                .isActive(false)
                .build();
        when(deviceRepository.findByUser(user)).thenReturn(List.of(inactiveDevice));

        assertThrows(DeviceException.class, () -> deviceService.updateFcmToken("owner", request));
    }

    @Test
    void checkNearbyMissingPersonsSendsAlertWhenEligible() {
        User user = createUser(60L, "Tracker");
        Point userLocation = createPoint(127.0, 37.5);
        MissingPerson missingPerson = MissingPerson.builder()
                .id(500L)
                .name("실종자")
                .location(createPoint(127.001, 37.501))
                .build();
        MissingCase missingCase = MissingCase.builder()
                .id(700L)
                .missingPerson(missingPerson)
                .caseStatus(CaseStatusType.OPEN)
                .reportedBy(createUser(61L, "Reporter"))
                .build();

        when(missingPersonRepository.findNearbyMissingPersons(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(missingPerson));
        when(missingCaseRepository.findByMissingPersonAndCaseStatus(missingPerson, CaseStatusType.OPEN))
                .thenReturn(Optional.of(missingCase));
        when(notificationRepository.findRecentNearbyAlerts(
                eq(user),
                eq(missingPerson.getId()),
                eq(NotificationType.NEARBY_ALERT),
                any()))
                .thenReturn(List.of());

        deviceService.checkNearbyMissingPersons(user, userLocation);

        verify(pushNotificationService).sendNearbyAlertToReporter(
                eq(user),
                eq(missingPerson.getName()),
                anyDouble(),
                eq(userLocation),
                eq(missingPerson.getId()));
    }

    @Test
    void checkNearbyMissingPersonsSkipsAlertWhenRecentNearbyNotificationExists() {
        User user = createUser(70L, "Tracker");
        Point userLocation = createPoint(127.0, 37.5);
        MissingPerson missingPerson = MissingPerson.builder()
                .id(800L)
                .name("실종자")
                .location(createPoint(127.01, 37.51))
                .build();
        Notification recentAlert = Notification.builder()
                .id(1L)
                .user(user)
                .type(NotificationType.NEARBY_ALERT)
                .relatedEntityId(missingPerson.getId())
                .relatedLocation(createPoint(127.001, 37.501))
                .createdAt(LocalDateTime.now())
                .build();

        when(missingPersonRepository.findNearbyMissingPersons(anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(missingPerson));
        when(notificationRepository.findRecentNearbyAlerts(
                eq(user),
                eq(missingPerson.getId()),
                eq(NotificationType.NEARBY_ALERT),
                any()))
                .thenReturn(List.of(recentAlert));

        deviceService.checkNearbyMissingPersons(user, userLocation);

        verify(pushNotificationService, never()).sendNearbyAlertToReporter(
                any(User.class), any(), anyDouble(), any(Point.class), anyLong());
        verify(missingCaseRepository, never())
                .findByMissingPersonAndCaseStatus(missingPerson, CaseStatusType.OPEN);
    }

    private DeviceRegisterRequest createRegisterRequest() {
        DeviceRegisterRequest request = new DeviceRegisterRequest();
        request.setDeviceUuid("device-uuid-123");
        request.setOsType("iOS");
        request.setOsVersion("17.0");
        request.setFcmToken("token");
        return request;
    }

    private GpsUpdateRequest createGpsRequest(double latitude, double longitude, Integer battery) {
        GpsUpdateRequest request = new GpsUpdateRequest();
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setBatteryLevel(battery);
        return request;
    }

    private User createUser(Long id, String name) {
        User user = User.builder()
                .uid("uid-" + id)
                .encodedPassword("encoded")
                .phoneE164("+8201012345678")
                .name(name)
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Device createDevice(Long id, User owner) {
        return Device.builder()
                .id(id)
                .user(owner)
                .deviceUuid("device-" + id)
                .osType("iOS")
                .osVersion("17")
                .isActive(true)
                .registeredAt(LocalDateTime.now())
                .fcmToken("fcm-" + id)
                .batteryLevel(50)
                .build();
    }

    private Point createPoint(double longitude, double latitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }
}
