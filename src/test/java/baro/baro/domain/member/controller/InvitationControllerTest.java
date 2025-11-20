package baro.baro.domain.member.controller;

import baro.baro.config.JwtAuthenticationFilter;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.member.dto.res.InvitationResponse;
import baro.baro.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = InvitationController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private MetricsService metricsService;

    @Test
    @DisplayName("멤버 초대 성공 시 201과 초대 정보를 반환한다")
    void invitations_success_returns201() throws Exception {
        // Given
        InvitationResponse response = new InvitationResponse(1L);
        when(memberService.makeInvitation(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/members/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "inviteeUserId", 2L,
                                "relation", "친구"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relationshipRequestId").value(1L));

        verify(memberService).makeInvitation(any());
    }
}
