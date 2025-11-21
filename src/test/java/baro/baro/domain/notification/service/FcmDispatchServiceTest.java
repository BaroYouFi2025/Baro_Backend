package baro.baro.domain.notification.service;

import baro.baro.domain.common.monitoring.MetricsService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmDispatchServiceTest {

    @Mock
    private MetricsService metricsService;

    private FcmDispatchService fcmDispatchService;

    @BeforeEach
    void setUp() {
        fcmDispatchService = new FcmDispatchService(metricsService);
    }

    @Test
    void dispatchRecordsFailureWhenFirebaseNotInitialized() {
        Message message = Message.builder().setToken("token").build();

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
            firebaseAppMock.when(FirebaseApp::getApps).thenReturn(List.of());

            fcmDispatchService.dispatch(message, "invitation", "token");

            verify(metricsService).recordFcmMessageFailure("invitation", "FIREBASE_NOT_INITIALIZED");
            verify(metricsService, never()).recordFcmMessageSuccess(anyString());
        }
    }

    @Test
    void dispatchSendsMessageAndRecordsMetrics() throws Exception {
        Message message = Message.builder().setToken("token").build();
        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class);
             MockedStatic<FirebaseMessaging> firebaseMessagingMock = mockStatic(FirebaseMessaging.class)) {
            firebaseAppMock.when(FirebaseApp::getApps).thenReturn(List.of(mock(FirebaseApp.class)));
            firebaseMessagingMock.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
            when(firebaseMessaging.send(message)).thenReturn("ok");

            fcmDispatchService.dispatch(message, "missing_person_found", "token");

            verify(firebaseMessaging).send(message);
            verify(metricsService).recordFcmMessageSuccess("missing_person_found");
            verify(metricsService).recordFcmSendDuration(anyLong());
        }
    }

    @Test
    void buildInvitationMessagePopulatesActionData() {
        Message message = fcmDispatchService.buildInvitationMessage(
                "token-1", "제목", "내용", 55L, "초대한 사람", "관계"
        );

        String token = (String) ReflectionTestUtils.getField(message, "token");
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) ReflectionTestUtils.getField(message, "data");

        assertThat(token).isEqualTo("token-1");
        assertThat(data)
                .containsEntry("type", "invitation")
                .containsEntry("invitationId", "55")
                .containsEntry("inviterName", "초대한 사람")
                .containsEntry("relation", "관계");
    }

    @Test
    void buildNearbyAlertMessageIncludesDistanceAndMissingPersonId() {
        Message message = fcmDispatchService.buildNearbyAlertMessage(
                "token-2", "근처", "알림", "실종자", "제보자", 42.5, 88L, "reporter"
        );

        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) ReflectionTestUtils.getField(message, "data");

        assertThat(data)
                .containsEntry("type", "nearby_alert")
                .containsEntry("missingPersonName", "실종자")
                .containsEntry("reporterName", "제보자")
                .containsEntry("distance", "42.5")
                .containsEntry("missingPersonId", "88")
                .containsEntry("recipientType", "reporter");
    }
}
