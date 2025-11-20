package baro.baro.domain.auth.controller;

import baro.baro.config.JwtAuthenticationFilter;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.service.AuthService;
import baro.baro.domain.common.monitoring.MetricsService;
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
        value = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private MetricsService metricsService;

    @Test
    @DisplayName("로그인 성공 시 access 토큰과 refresh 토큰을 반환한다")
    void login_success_returnsTokens() throws Exception {
        // Given
        AuthTokensResponse response = new AuthTokensResponse(
                "access-token-12345",
                "refresh-token-67890",
                3600L
        );
        when(authService.login(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "uid", "user001",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-12345"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-67890"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(authService).login(any());
    }

    @Test
    @DisplayName("로그인 시 uid가 누락되면 400 에러를 반환한다")
    void login_withoutUid_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "password", "password123"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 시 password가 누락되면 400 에러를 반환한다")
    void login_withoutPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "uid", "user001"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그아웃 성공 시 메시지를 반환한다")
    void logout_success_returnsMessage() throws Exception {
        // Given
        LogoutResponse response = new LogoutResponse("로그아웃 되었습니다.");
        when(authService.logout(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", "refresh-token-12345"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));

        verify(authService).logout("refresh-token-12345");
    }

    @Test
    @DisplayName("로그아웃 시 refreshToken이 누락되면 400 에러를 반환한다")
    void logout_withoutRefreshToken_returns400() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 갱신 성공 시 새로운 토큰들을 반환한다")
    void refresh_success_returnsNewTokens() throws Exception {
        // Given
        RefreshResponse response = new RefreshResponse(
                "new-access-token",
                "new-refresh-token",
                3600L
        );
        when(authService.refresh(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", "old-refresh-token"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));

        verify(authService).refresh("old-refresh-token");
    }

    @Test
    @DisplayName("토큰 갱신 시 refreshToken이 누락되면 400 에러를 반환한다")
    void refresh_withoutRefreshToken_returns400() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
