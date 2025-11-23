package baro.baro.domain.notification.service;

import baro.baro.domain.common.util.SecurityUtil;
import baro.baro.domain.member.dto.req.AcceptInvitationRequest;
import baro.baro.domain.member.dto.req.RejectInvitationRequest;
import baro.baro.domain.member.dto.res.AcceptInvitationResponse;
import baro.baro.domain.member.service.MemberService;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.dto.res.SightingDetailResponse;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.repository.SightingRepository;
import baro.baro.domain.missingperson.service.MissingPersonService;
import baro.baro.domain.notification.dto.res.NotificationResponse;
import baro.baro.domain.notification.entity.Notification;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.notification.exception.NotificationException;
import baro.baro.domain.notification.repository.NotificationRepository;
import baro.baro.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private MissingPersonService missingPersonService;
    @Mock
    private SightingRepository sightingRepository;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                notificationRepository,
                memberService,
                missingPersonService,
                sightingRepository
        );
    }

    @Test
    void getMyNotificationsReturnsMappedResponses() {
        User currentUser = createUser(1L, "알림 사용자");
        Notification first = createNotification(10L, currentUser, NotificationType.INVITE_REQUEST, false, 100L);
        Notification second = createNotification(11L, currentUser, NotificationType.FOUND_REPORT, true, 101L);
        when(notificationRepository.findByUserOrderByCreatedAtDesc(currentUser))
                .thenReturn(List.of(first, second));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            List<NotificationResponse> responses = notificationService.getMyNotifications();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getId()).isEqualTo(first.getId());
            assertThat(responses.get(1).getType()).isEqualTo(NotificationType.FOUND_REPORT);
        }
    }

    @Test
    void getUnreadNotificationsReturnsOnlyUnreadResponses() {
        User currentUser = createUser(2L, "유저");
        Notification unread = createNotification(20L, currentUser, NotificationType.NEARBY_ALERT, false, 200L);
        when(notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(currentUser))
                .thenReturn(List.of(unread));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            List<NotificationResponse> responses = notificationService.getUnreadNotifications();

            assertThat(responses).singleElement().satisfies(it -> {
                assertThat(it.getId()).isEqualTo(unread.getId());
                assertThat(it.isRead()).isFalse();
            });
        }
    }

    @Test
    void getUnreadCountDelegatesToRepository() {
        User currentUser = createUser(3L, "카운터");
        when(notificationRepository.countUnreadByUser(currentUser)).thenReturn(3L);

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            long result = notificationService.getUnreadCount();

            assertThat(result).isEqualTo(3L);
        }
    }

    @Test
    void markAsReadUpdatesNotificationForOwner() {
        User currentUser = createUser(4L, "소유자");
        Notification notification = createNotification(30L, currentUser, NotificationType.INVITE_REQUEST, false, 300L);
        when(notificationRepository.findById(30L)).thenReturn(Optional.of(notification));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            notificationService.markAsRead(30L);
        }

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadThrowsWhenNotificationBelongsToAnotherUser() {
        User currentUser = createUser(5L, "사용자");
        User otherUser = createUser(6L, "다른 사용자");
        Notification notification = createNotification(31L, otherUser, NotificationType.NEARBY_ALERT, false, 301L);
        when(notificationRepository.findById(31L)).thenReturn(Optional.of(notification));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            assertThatThrownBy(() -> notificationService.markAsRead(31L))
                    .isInstanceOf(NotificationException.class);
        }
    }

    @Test
    void acceptInvitationFromNotificationMarksAsReadAndDelegates() {
        User currentUser = createUser(7L, "수신자");
        Notification notification = createNotification(40L, currentUser, NotificationType.INVITE_REQUEST, false, 400L);
        when(notificationRepository.findById(40L)).thenReturn(Optional.of(notification));
        AcceptInvitationResponse response = AcceptInvitationResponse.of(1L, 2L);
        when(memberService.acceptInvitation(any(AcceptInvitationRequest.class))).thenReturn(response);

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            AcceptInvitationResponse result = notificationService.acceptInvitationFromNotification(40L, "아버지");

            assertThat(result).isSameAs(response);
        }

        ArgumentCaptor<AcceptInvitationRequest> requestCaptor = ArgumentCaptor.forClass(AcceptInvitationRequest.class);
        verify(memberService).acceptInvitation(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRelationshipRequestId()).isEqualTo(400L);
        assertThat(requestCaptor.getValue().getRelation()).isEqualTo("아버지");
        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void acceptInvitationFromNotificationRejectsUnexpectedType() {
        User currentUser = createUser(8L, "수신자");
        Notification notification = createNotification(41L, currentUser, NotificationType.FOUND_REPORT, false, 401L);
        when(notificationRepository.findById(41L)).thenReturn(Optional.of(notification));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            assertThatThrownBy(() -> notificationService.acceptInvitationFromNotification(41L, "관계"))
                    .isInstanceOf(NotificationException.class);
        }
    }

    @Test
    void rejectInvitationFromNotificationDelegatesWithRelatedId() {
        User currentUser = createUser(9L, "테스터");
        Notification notification = createNotification(50L, currentUser, NotificationType.INVITE_REQUEST, false, 500L);
        when(notificationRepository.findById(50L)).thenReturn(Optional.of(notification));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            notificationService.rejectInvitationFromNotification(50L);
        }

        ArgumentCaptor<RejectInvitationRequest> captor = ArgumentCaptor.forClass(RejectInvitationRequest.class);
        verify(memberService).rejectInvitation(captor.capture());
        assertThat(captor.getValue().getRelationshipId()).isEqualTo(500L);
        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void rejectInvitationFromNotificationThrowsWhenRelatedEntityMissing() {
        User currentUser = createUser(10L, "테스터");
        Notification notification = createNotification(51L, currentUser, NotificationType.INVITE_REQUEST, false, null);
        when(notificationRepository.findById(51L)).thenReturn(Optional.of(notification));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            assertThatThrownBy(() -> notificationService.rejectInvitationFromNotification(51L))
                    .isInstanceOf(NotificationException.class);
        }
    }

    @Test
    void getMissingPersonDetailFromNotificationReturnsServiceResponse() {
        User currentUser = createUser(11L, "사용자");
        Notification notification = createNotification(60L, currentUser, NotificationType.FOUND_REPORT, false, 600L);
        when(notificationRepository.findById(60L)).thenReturn(Optional.of(notification));
        MissingPersonDetailResponse response = MissingPersonDetailResponse.create(
                600L, "홍길동", "2010-01-01", "부산", "2024-01-01T00:00:00",
                120, 30, "마름", "특이사항", "파란", "검정", "모자", 37.5, 126.9, "http://photo", null, null
        );
        when(missingPersonService.getMissingPersonDetail(600L)).thenReturn(response);

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            MissingPersonDetailResponse result = notificationService.getMissingPersonDetailFromNotification(60L);

            assertThat(result).isSameAs(response);
        }
    }

    @Test
    void getMissingPersonDetailFromNotificationRejectsInvalidType() {
        User currentUser = createUser(12L, "사용자");
        Notification notification = createNotification(61L, currentUser, NotificationType.INVITE_REQUEST, false, 601L);
        when(notificationRepository.findById(61L)).thenReturn(Optional.of(notification));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            assertThatThrownBy(() -> notificationService.getMissingPersonDetailFromNotification(61L))
                    .isInstanceOf(NotificationException.class);
        }
    }

    @Test
    void getSightingDetailFromNotificationReturnsDto() {
        User currentUser = createUser(13L, "사용자");
        Notification notification = createNotification(70L, currentUser, NotificationType.FOUND_REPORT, false, 700L);
        when(notificationRepository.findById(70L)).thenReturn(Optional.of(notification));

        SightingDetailResponse expectedResponse = createSightingDetailResponse();
        when(sightingRepository.findById(700L)).thenReturn(Optional.of(createStubSighting(expectedResponse)));

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            SightingDetailResponse response = notificationService.getSightingDetailFromNotification(70L);

            assertThat(response.getSightingId()).isEqualTo(expectedResponse.getSightingId());
            assertThat(response.getReporterName()).isEqualTo(expectedResponse.getReporterName());
        }
    }

    @Test
    void getSightingDetailFromNotificationThrowsWhenNotFound() {
        User currentUser = createUser(14L, "사용자");
        Notification notification = createNotification(71L, currentUser, NotificationType.FOUND_REPORT, false, 701L);
        when(notificationRepository.findById(71L)).thenReturn(Optional.of(notification));
        when(sightingRepository.findById(701L)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> mocked = mockCurrentUser(currentUser)) {
            assertThatThrownBy(() -> notificationService.getSightingDetailFromNotification(71L))
                    .isInstanceOf(NotificationException.class);
        }
    }

    private Notification createNotification(Long id, User user, NotificationType type, boolean isRead, Long relatedId) {
        return Notification.builder()
                .id(id)
                .user(user)
                .type(type)
                .title("title-" + id)
                .message("message-" + id)
                .isRead(isRead)
                .createdAt(LocalDateTime.now())
                .relatedEntityId(relatedId)
                .build();
    }

    private User createUser(Long id, String name) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "name", name);
        return user;
    }

    private MockedStatic<SecurityUtil> mockCurrentUser(User user) {
        MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class);
        mocked.when(SecurityUtil::getCurrentUser).thenReturn(user);
        return mocked;
    }

    private SightingDetailResponse createSightingDetailResponse() {
        SightingDetailResponse response = new SightingDetailResponse();
        ReflectionTestUtils.setField(response, "sightingId", 700L);
        ReflectionTestUtils.setField(response, "missingPersonId", 701L);
        ReflectionTestUtils.setField(response, "missingPersonName", "실종자");
        ReflectionTestUtils.setField(response, "reporterName", "제보자");
        ReflectionTestUtils.setField(response, "latitude", 37.0);
        ReflectionTestUtils.setField(response, "longitude", 127.0);
        ReflectionTestUtils.setField(response, "address", "서울");
        ReflectionTestUtils.setField(response, "reportedAt", ZonedDateTime.now());
        return response;
    }

    private SightingStub createStubSighting(SightingDetailResponse response) {
        return new SightingStub(response);
    }

    private static final class SightingStub extends baro.baro.domain.missingperson.entity.Sighting {
        private final SightingDetailResponse response;
        private final MissingCase missingCase;
        private final User reporter;

        private SightingStub(SightingDetailResponse response) {
            this.response = response;
            this.reporter = createReporter(response);
            MissingPerson person = MissingPerson.builder()
                    .id(response.getMissingPersonId())
                    .name(response.getMissingPersonName())
                    .missingDate(LocalDateTime.now())
                    .build();
            this.missingCase = MissingCase.builder()
                    .id(99L)
                    .missingPerson(person)
                    .reportedBy(this.reporter)
                    .reportedAt(ZonedDateTime.now())
                    .build();
        }

        @Override
        public Long getId() {
            return response.getSightingId();
        }

        @Override
        public MissingCase getMissingCase() {
            return missingCase;
        }

        @Override
        public User getReporter() {
            return reporter;
        }

        @Override
        public Double getLatitude() {
            return response.getLatitude();
        }

        @Override
        public Double getLongitude() {
            return response.getLongitude();
        }

        @Override
        public String getAddress() {
            return response.getAddress();
        }

        @Override
        public ZonedDateTime getCreatedAt() {
            return response.getReportedAt();
        }

        private static User createReporter(SightingDetailResponse response) {
            User reporter = new User();
            ReflectionTestUtils.setField(reporter, "id", 888L);
            ReflectionTestUtils.setField(reporter, "name", response.getReporterName());
            return reporter;
        }
    }
}
