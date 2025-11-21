package baro.baro.domain.notification.controller;

import baro.baro.domain.notification.dto.res.NotificationResponse;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.notification.service.NotificationServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationServiceInterface notificationService;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
    }

    @Test
    void getMyNotificationsReturnsList() throws Exception {
        NotificationResponse response = createNotificationResponse(1L, "INVITE_REQUEST");
        when(notificationService.getMyNotifications()).thenReturn(List.of(response));

        mockMvc.perform(get("/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].type").value("INVITE_REQUEST"));
    }

    @Test
    void getUnreadNotificationsReturnsList() throws Exception {
        NotificationResponse response = createNotificationResponse(2L, "NEARBY_ALERT");
        when(notificationService.getUnreadNotifications()).thenReturn(List.of(response));

        mockMvc.perform(get("/notifications/me/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("NEARBY_ALERT"));
    }

    @Test
    void getUnreadCountReturnsNumber() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(3L);

        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));
    }

    @Test
    void markAsReadInvokesService() throws Exception {
        mockMvc.perform(put("/notifications/{id}/read", 5L))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(5L);
    }

    private NotificationResponse createNotificationResponse(Long id, String type) {
        NotificationResponse response = new NotificationResponse();
        ReflectionTestUtils.setField(response, "id", id);
        ReflectionTestUtils.setField(response, "type", NotificationType.valueOf(type));
        ReflectionTestUtils.setField(response, "title", "제목");
        ReflectionTestUtils.setField(response, "message", "내용");
        return response;
    }
}
