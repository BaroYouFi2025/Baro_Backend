package baro.baro.domain.notification.service;

import baro.baro.domain.notification.entity.Notification;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.notification.repository.NotificationRepository;
import baro.baro.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationPersistenceServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationPersistenceService notificationPersistenceService;

    @BeforeEach
    void setUp() {
        notificationPersistenceService = new NotificationPersistenceService(notificationRepository);
    }

    @Test
    void savePersistsNotificationWithoutLocation() {
        User user = createUser(1L, "수신자");

        notificationPersistenceService.save(user, NotificationType.FOUND_REPORT,
                "제목", "내용", 99L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getType()).isEqualTo(NotificationType.FOUND_REPORT);
        assertThat(saved.getTitle()).isEqualTo("제목");
        assertThat(saved.getMessage()).isEqualTo("내용");
        assertThat(saved.getRelatedEntityId()).isEqualTo(99L);
        assertThat(saved.getRelatedLocation()).isNull();
        assertThat(saved.isRead()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void saveWithLocationPersistsPointAndMessage() {
        User user = createUser(2L, "GPS 사용자");
        Point point = new GeometryFactory().createPoint(new Coordinate(127.0, 37.5));

        notificationPersistenceService.saveWithLocation(user, NotificationType.NEARBY_ALERT,
                "근처 알림", "근처에 실종자가 있습니다", 300L, point);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getType()).isEqualTo(NotificationType.NEARBY_ALERT);
        assertThat(saved.getRelatedEntityId()).isEqualTo(300L);
        assertThat(saved.getRelatedLocation()).isEqualTo(point);
        assertThat(saved.isRead()).isFalse();
    }

    private User createUser(Long id, String name) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "name", name);
        return user;
    }
}
