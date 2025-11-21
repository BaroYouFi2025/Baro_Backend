package baro.baro.domain.missingperson.service;

import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.ReportSightingRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.ReportSightingResponse;
import baro.baro.domain.missingperson.entity.CaseStatusType;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.entity.Sighting;
import baro.baro.domain.missingperson.exception.MissingPersonErrorCode;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import baro.baro.domain.missingperson.repository.MissingCaseRepository;
import baro.baro.domain.missingperson.repository.MissingPersonRepository;
import baro.baro.domain.missingperson.repository.SightingRepository;
import baro.baro.domain.notification.exception.NotificationErrorCode;
import baro.baro.domain.notification.exception.NotificationException;
import baro.baro.domain.notification.service.PushNotificationService;
import baro.baro.domain.user.entity.User;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissingPersonServiceImplTest {

    @Mock
    private MissingPersonRepository missingPersonRepository;

    @Mock
    private MissingCaseRepository missingCaseRepository;

    @Mock
    private SightingRepository sightingRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private MetricsService metricsService;

    private MissingPersonService missingPersonService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @BeforeEach
    void setUp() {
        missingPersonService = new MissingPersonServiceImpl(
                missingPersonRepository,
                missingCaseRepository,
                sightingRepository,
                locationService,
                pushNotificationService,
                metricsService
        );
    }

    @Test
    void registerMissingPersonPersistsEntitiesAndRecordsMetrics() {
        RegisterMissingPersonRequest request = createRegisterRequest();
        User currentUser = createUser(1L, "Reporter");
        Point point = createPoint(127.0, 37.5);
        LocationService.LocationInfo locationInfo = new LocationService.LocationInfo("Seoul", point);

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            when(missingCaseRepository.countByReportedById(currentUser.getId())).thenReturn(2L);
            when(locationService.createLocationInfo(request.getLatitude(), request.getLongitude()))
                    .thenReturn(locationInfo);
            when(missingPersonRepository.save(any(MissingPerson.class))).thenReturn(
                    MissingPerson.builder()
                            .id(10L)
                            .name(request.getName())
                            .build()
            );

            RegisterMissingPersonResponse response = missingPersonService.registerMissingPerson(request);

            assertThat(response.getMissingPersonId()).isEqualTo(10L);
            verify(locationService).createLocationInfo(request.getLatitude(), request.getLongitude());
            verify(missingPersonRepository).save(any(MissingPerson.class));
            verify(missingCaseRepository).save(any(MissingCase.class));
            verify(metricsService).recordMissingPersonReport();
        }
    }

    @Test
    void registerMissingPersonThrowsWhenLimitExceeded() {
        RegisterMissingPersonRequest request = createRegisterRequest();
        User currentUser = createUser(2L, "Reporter");

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            when(missingCaseRepository.countByReportedById(currentUser.getId())).thenReturn(4L);

            MissingPersonException exception = assertThrows(
                    MissingPersonException.class,
                    () -> missingPersonService.registerMissingPerson(request)
            );

            assertThat(exception.getMissingPersonErrorCode())
                    .isEqualTo(MissingPersonErrorCode.MISSING_PERSON_LIMIT_EXCEEDED);
            verify(missingPersonRepository, never()).save(any(MissingPerson.class));
            verify(metricsService, never()).recordMissingPersonReport();
        }
    }

    @Test
    void searchMissingPersonsMapsResultsToResponses() {
        SearchMissingPersonRequest request = SearchMissingPersonRequest.create(0, 5);
        MissingPerson first = createMissingPersonEntity(10L, "홍길동");
        MissingPerson second = createMissingPersonEntity(11L, "김철수");
        Page<MissingPerson> page = new PageImpl<>(List.of(first, second), PageRequest.of(0, 5), 2);
        when(missingPersonRepository.findAllOpenCases(CaseStatusType.OPEN, PageRequest.of(0, 5)))
                .thenReturn(page);

        Page<?> result = missingPersonService.searchMissingPersons(request);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).first().hasFieldOrPropertyWithValue("name", "홍길동");
    }

    @Test
    void getMyMissingPersonsReturnsResponsesForCurrentUser() {
        User currentUser = createUser(30L, "등록자");
        MissingPerson entity = createMissingPersonEntity(20L, "내 실종자");
        when(missingPersonRepository.findAllByReporterId(currentUser.getId(), CaseStatusType.OPEN))
                .thenReturn(List.of(entity));

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);

            List<?> responses = missingPersonService.getMyMissingPersons();

            assertThat(responses).singleElement().hasFieldOrPropertyWithValue("missingPersonId", 20L);
        }
    }

    @Test
    void findNearbyMissingPersonsReturnsPageOfResponses() {
        NearbyMissingPersonRequest request = NearbyMissingPersonRequest.create(37.5, 127.0, 1000);
        MissingPerson nearby = createMissingPersonEntity(50L, "근처 실종자");
        when(missingPersonRepository.findNearbyMissingPersons(
                request.getLatitude(), request.getLongitude(), request.getRadius()
        )).thenReturn(List.of(nearby));

        Page<?> page = missingPersonService.findNearbyMissingPersons(request);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).first().hasFieldOrPropertyWithValue("name", "근처 실종자");
    }

    @Test
    void findNearbyMissingPersonsThrowsForInvalidCoordinates() {
        NearbyMissingPersonRequest request = NearbyMissingPersonRequest.create(120.0, 127.0, 500);

        assertThrows(IllegalArgumentException.class,
                () -> missingPersonService.findNearbyMissingPersons(request));
        verify(missingPersonRepository, never()).findNearbyMissingPersons(any(), any(), any());
    }

    @Test
    void getMissingPersonDetailReturnsResponse() {
        MissingPerson entity = createMissingPersonEntity(70L, "상세조회");
        when(missingPersonRepository.findById(70L)).thenReturn(Optional.of(entity));

        assertThat(missingPersonService.getMissingPersonDetail(70L).getName()).isEqualTo("상세조회");
    }

    @Test
    void getMissingPersonDetailThrowsWhenNotFound() {
        when(missingPersonRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(MissingPersonException.class,
                () -> missingPersonService.getMissingPersonDetail(999L));
    }

    @Test
    void updateMissingPersonResolvesLocationWhenCoordinatesProvided() {
        UpdateMissingPersonRequest request = UpdateMissingPersonRequest.create(
                "홍길동",
                "2010-05-15",
                "https://example.com/photo.jpg",
                "2024-01-01T12:00:00",
                150,
                45,
                "마른 편",
                "왼쪽 팔에 점",
                "파란색 셔츠",
                "청바지",
                "빨간 신발",
                37.5,
                126.9
        );
        MissingPerson missingPerson = Mockito.mock(MissingPerson.class);
        when(missingPerson.getName()).thenReturn("홍길동");
        when(missingPersonRepository.findById(5L)).thenReturn(java.util.Optional.of(missingPerson));

        Point point = createPoint(126.9, 37.5);
        LocationService.LocationInfo locationInfo = new LocationService.LocationInfo("서울시 중구", point);
        when(locationService.createLocationInfo(request.getLatitude(), request.getLongitude()))
                .thenReturn(locationInfo);

        missingPersonService.updateMissingPerson(5L, request);

        verify(locationService).createLocationInfo(request.getLatitude(), request.getLongitude());
        verify(missingPerson).updateFrom(
                eq(request.getName()),
                eq(request.getBirthDate()),
                eq(request.getBody()),
                eq(request.getBodyEtc()),
                eq(request.getClothesTop()),
                eq(request.getClothesBottom()),
                eq(request.getClothesEtc()),
                eq(request.getHeight()),
                eq(request.getWeight()),
                eq(locationInfo.point()),
                eq(locationInfo.address()),
                eq(request.getMissingDate())
        );
    }

    @Test
    void updateMissingPersonThrowsWhenNotFound() {
        when(missingPersonRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MissingPersonException.class,
                () -> missingPersonService.updateMissingPerson(99L,
                        UpdateMissingPersonRequest.create(null, null, null, null, null, null,
                                null, null, null, null, null, null, null)));
    }

    @Test
    void reportSightingThrowsWhenDuplicateWithinWindow() {
        ReportSightingRequest request = ReportSightingRequest.builder()
                .missingPersonId(3L)
                .latitude(37.1)
                .longitude(127.1)
                .build();

        User currentUser = createUser(3L, "Reporter");
        User owner = createUser(4L, "Owner");
        MissingPerson missingPerson = MissingPerson.builder()
                .id(3L)
                .name("실종자")
                .build();
        MissingCase missingCase = MissingCase.builder()
                .id(10L)
                .caseStatus(CaseStatusType.OPEN)
                .missingPerson(missingPerson)
                .reportedBy(owner)
                .build();

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            when(missingPersonRepository.findById(request.getMissingPersonId()))
                    .thenReturn(java.util.Optional.of(missingPerson));
            when(missingCaseRepository.findByMissingPersonAndCaseStatus(missingPerson, CaseStatusType.OPEN))
                    .thenReturn(java.util.Optional.of(missingCase));
            when(sightingRepository.existsRecentSightingByReporter(
                    eq(missingCase),
                    eq(currentUser),
                    any()))
                    .thenReturn(true);

            MissingPersonException exception = assertThrows(
                    MissingPersonException.class,
                    () -> missingPersonService.reportSighting(request)
            );

            assertThat(exception.getMissingPersonErrorCode())
                    .isEqualTo(MissingPersonErrorCode.DUPLICATE_SIGHTING_REPORT);
            verify(locationService, never()).createLocationInfo(any(), any());
        }
    }

    @Test
    void reportSightingThrowsWhenMissingPersonNotFound() {
        ReportSightingRequest request = ReportSightingRequest.builder()
                .missingPersonId(99L)
                .latitude(37.1)
                .longitude(127.1)
                .build();

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(createUser(50L, "요청자"));
            when(missingPersonRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(MissingPersonException.class,
                    () -> missingPersonService.reportSighting(request));
            verify(missingCaseRepository, never()).findByMissingPersonAndCaseStatus(any(), any());
        }
    }

    @Test
    void reportSightingThrowsWhenNoActiveCaseFound() {
        ReportSightingRequest request = ReportSightingRequest.builder()
                .missingPersonId(3L)
                .latitude(37.1)
                .longitude(127.1)
                .build();

        MissingPerson missingPerson = MissingPerson.builder()
                .id(3L)
                .name("실종자")
                .build();

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(createUser(51L, "요청자"));
            when(missingPersonRepository.findById(3L)).thenReturn(Optional.of(missingPerson));
            when(missingCaseRepository.findByMissingPersonAndCaseStatus(missingPerson, CaseStatusType.OPEN))
                    .thenReturn(Optional.empty());

            assertThrows(MissingPersonException.class,
                    () -> missingPersonService.reportSighting(request));
        }
    }

    @Test
    void reportSightingThrowsWhenCaseAlreadyClosed() {
        ReportSightingRequest request = ReportSightingRequest.builder()
                .missingPersonId(4L)
                .latitude(37.1)
                .longitude(127.1)
                .build();

        MissingPerson missingPerson = MissingPerson.builder()
                .id(4L)
                .name("실종자")
                .build();
        MissingCase missingCase = MissingCase.builder()
                .id(30L)
                .missingPerson(missingPerson)
                .caseStatus(CaseStatusType.CLOSED)
                .reportedBy(createUser(9L, "등록자"))
                .build();

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(createUser(52L, "신고자"));
            when(missingPersonRepository.findById(4L)).thenReturn(Optional.of(missingPerson));
            when(missingCaseRepository.findByMissingPersonAndCaseStatus(missingPerson, CaseStatusType.OPEN))
                    .thenReturn(Optional.of(missingCase));

            assertThrows(MissingPersonException.class,
                    () -> missingPersonService.reportSighting(request));
        }
    }

    @Test
    void reportSightingStillSucceedsWhenNotificationFails() throws FirebaseMessagingException {
        ReportSightingRequest request = ReportSightingRequest.builder()
                .missingPersonId(7L)
                .latitude(37.55)
                .longitude(126.98)
                .build();

        User currentUser = createUser(7L, "Reporter");
        User owner = createUser(8L, "Owner");
        MissingPerson missingPerson = MissingPerson.builder()
                .id(7L)
                .name("실종자")
                .build();
        MissingCase missingCase = MissingCase.builder()
                .id(20L)
                .caseStatus(CaseStatusType.OPEN)
                .missingPerson(missingPerson)
                .reportedBy(owner)
                .build();

        Point point = createPoint(126.98, 37.55);
        LocationService.LocationInfo locationInfo = new LocationService.LocationInfo("서울시 종로구", point);
        Sighting savedSighting = Sighting.builder()
                .id(99L)
                .missingCase(missingCase)
                .reporter(currentUser)
                .location(point)
                .address(locationInfo.address())
                .build();

        try (MockedStatic<SecurityUtil> mockedStatic = Mockito.mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUser).thenReturn(currentUser);
            when(missingPersonRepository.findById(request.getMissingPersonId()))
                    .thenReturn(java.util.Optional.of(missingPerson));
            when(missingCaseRepository.findByMissingPersonAndCaseStatus(missingPerson, CaseStatusType.OPEN))
                    .thenReturn(java.util.Optional.of(missingCase));
            when(sightingRepository.existsRecentSightingByReporter(eq(missingCase), eq(currentUser), any()))
                    .thenReturn(false);
            when(locationService.createLocationInfo(request.getLatitude(), request.getLongitude()))
                    .thenReturn(locationInfo);
            when(sightingRepository.save(any(Sighting.class))).thenReturn(savedSighting);
            doThrow(new NotificationException(NotificationErrorCode.SIGHTING_NOT_FOUND))
                    .when(pushNotificationService)
                    .sendMissingPersonFoundNotification(
                            savedSighting.getId(),
                            owner,
                            missingPerson.getName(),
                            currentUser.getName(),
                            locationInfo.address()
                    );

            ReportSightingResponse response = missingPersonService.reportSighting(request);

            assertThat(response.getMessage()).contains("신고가 접수");
            verify(locationService).createLocationInfo(request.getLatitude(), request.getLongitude());
            verify(sightingRepository).save(any(Sighting.class));
            verify(metricsService).recordMissingPersonFound();
        }
    }

    private RegisterMissingPersonRequest createRegisterRequest() {
        return RegisterMissingPersonRequest.create(
                "홍길동",
                "2010-05-15",
                "https://example.com/photo.jpg",
                "2024-01-01T12:00:00",
                150,
                40,
                "마른 체형",
                "이마에 흉터",
                "파란 셔츠",
                "청바지",
                "빨간 신발",
                37.5,
                127.0,
                "MALE"
        );
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

    private MissingPerson createMissingPersonEntity(Long id, String name) {
        MissingPerson missingPerson = MissingPerson.builder()
                .name(name)
                .address("주소")
                .missingDate(LocalDateTime.now())
                .location(createPoint(127.0, 37.5))
                .build();
        ReflectionTestUtils.setField(missingPerson, "id", id);
        ReflectionTestUtils.setField(missingPerson, "height", 150);
        ReflectionTestUtils.setField(missingPerson, "weight", 45);
        ReflectionTestUtils.setField(missingPerson, "body", "체형");
        return missingPerson;
    }

    private Point createPoint(double longitude, double latitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }
}
