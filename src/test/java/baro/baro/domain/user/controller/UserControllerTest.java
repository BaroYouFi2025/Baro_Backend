package baro.baro.domain.user.controller;

import baro.baro.config.JwtAuthenticationFilter;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.user.dto.req.DeleteUserRequest;
import baro.baro.domain.user.dto.req.UpdateProfileRequest;
import baro.baro.domain.user.dto.req.UserSearchRequest;
import baro.baro.domain.user.dto.res.DeleteUserResponse;
import baro.baro.domain.user.dto.res.UserProfileResponse;
import baro.baro.domain.user.dto.res.UserPublicProfileResponse;
import baro.baro.domain.user.service.UserService;
import baro.baro.domain.common.monitoring.MetricsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = UserController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private MetricsService metricsService;

    @Test
    void signupReturnsTokens() throws Exception {
        AuthTokensResponse tokens = new AuthTokensResponse("access", "refresh", 3600);
        when(userService.signup(any(), any())).thenReturn(tokens);

        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "uid", "newUser",
                                "password", "Password12!",
                                "phone", "01012345678",
                                "username", "New User",
                                "birthDate", "2000-01-01"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));

        verify(userService).signup(any(), any());
    }

    @Test
    void getProfileReturnsResponse() throws Exception {
        UserProfileResponse profile = UserProfileResponse.create(1L, "Tester", 2, 10, "Pro", "https://img", "#fff");
        when(userService.getProfile()).thenReturn(profile);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tester"))
                .andExpect(jsonPath("$.title").value("Pro"));

        verify(userService).getProfile();
    }

    @Test
    void updateProfileReturnsUpdatedBody() throws Exception {
        UserProfileResponse updated = UserProfileResponse.create(1L, "Renamed", 3, 20, "Explorer", "https://img/new", "#000");
        when(userService.updateProfile(any(UpdateProfileRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Renamed",
                                "title", "Explorer"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.title").value("Explorer"));

        verify(userService).updateProfile(any(UpdateProfileRequest.class));
    }

    @Test
    void deleteUserReturnsConfirmation() throws Exception {
        DeleteUserResponse response = DeleteUserResponse.create("탈퇴 완료");
        when(userService.deleteUser(any(DeleteUserRequest.class))).thenReturn(response);

        mockMvc.perform(delete("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("탈퇴 완료"));

        verify(userService).deleteUser(any(DeleteUserRequest.class));
    }

    @Test
    void searchUsersWithoutBodyUsesDefaultRequest() throws Exception {
        Slice<UserPublicProfileResponse> slice = new SliceImpl<>(
                List.of(UserPublicProfileResponse.builder()
                        .uid("friend")
                        .name("친구")
                        .profileUrl("https://img")
                        .profileBackgroundColor("#fff")
                        .build()),
                PageRequest.of(0, 20),
                false
        );
        when(userService.searchUsers(any(UserSearchRequest.class))).thenReturn(slice);

        mockMvc.perform(post("/users/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uid").value("friend"));

        ArgumentCaptor<UserSearchRequest> captor = ArgumentCaptor.forClass(UserSearchRequest.class);
        verify(userService).searchUsers(captor.capture());
        assertThat(captor.getValue().getPage()).isEqualTo(0);
        assertThat(captor.getValue().getSize()).isEqualTo(20);
    }
}
