package baro.baro.domain.notification.controller;

import baro.baro.domain.member.dto.res.AcceptInvitationResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.dto.res.SightingDetailResponse;
import baro.baro.domain.notification.dto.req.AcceptInvitationFromNotificationRequest;
import baro.baro.domain.notification.service.NotificationServiceInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationActionControllerTest {

    @InjectMocks
    private NotificationActionController notificationActionController;

    @Mock
    private NotificationServiceInterface notificationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(notificationActionController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void acceptInvitationFromNotificationReturnsResponse() throws Exception {
        AcceptInvitationResponse response = AcceptInvitationResponse.of(1L, 2L);
        when(notificationService.acceptInvitationFromNotification(anyLong(), anyString())).thenReturn(response);
        AcceptInvitationFromNotificationRequest request = new AcceptInvitationFromNotificationRequest();
        request.setRelation("아버지");

        mockMvc.perform(post("/notifications/{id}/accept-invitation", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.relationshipIds[0]").value(1L));

        verify(notificationService).acceptInvitationFromNotification(5L, "아버지");
    }

    @Test
    void rejectInvitationFromNotificationReturnsOk() throws Exception {
        mockMvc.perform(post("/notifications/{id}/reject-invitation", 6L))
                .andExpect(status().isOk());

        verify(notificationService).rejectInvitationFromNotification(6L);
    }

    @Test
    void getMissingPersonDetailReturnsPayload() throws Exception {
        MissingPersonDetailResponse detail = MissingPersonDetailResponse.create(
                10L, "홍길동", "2010-01-01", "부산",
                "2024-01-01T00:00:00", 150, 40, "마른", "흉터",
                "파랑", "청바지", "모자", 37.5, 127.0, "http://photo"
        );
        when(notificationService.getMissingPersonDetailFromNotification(7L)).thenReturn(detail);

        mockMvc.perform(get("/notifications/{id}/missing-person", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    void getSightingDetailReturnsPayload() throws Exception {
        SightingDetailResponse response = new SightingDetailResponse();
        ReflectionTestUtils.setField(response, "sightingId", 9L);
        ReflectionTestUtils.setField(response, "reporterName", "제보자");
        when(notificationService.getSightingDetailFromNotification(8L)).thenReturn(response);

        mockMvc.perform(get("/notifications/{id}/sighting", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sightingId").value(9L));
    }
}
