package baro.baro.domain.auth.service;

import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.entity.BlacklistedToken;
import baro.baro.domain.auth.exception.AuthException;
import baro.baro.domain.auth.repository.BlacklistedTokenRepository;
import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.common.monitoring.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private MetricsService metricsService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                jwtTokenProvider,
                blacklistedTokenRepository,
                deviceRepository,
                metricsService,
                passwordEncoder
        );
        ReflectionTestUtils.setField(authService, "cookieSecure", false);
    }

    @Test
    void loginReturnsTokensWhenCredentialsValid() {
        LoginRequest request = new LoginRequest();
        request.setUid("user");
        request.setPassword("password");

        User activeUser = createUser("user", true);
        when(userRepository.findByUid("user")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password", activeUser.getPasswordHash())).thenReturn(true);
        Device activeDevice = createDevice(10L, true);
        when(deviceRepository.findByUser(activeUser)).thenReturn(List.of(activeDevice, createDevice(11L, false)));
        when(jwtTokenProvider.createAccessToken(eq("user"), anyString(), eq(10L))).thenReturn("access");
        when(jwtTokenProvider.createRefreshToken("user")).thenReturn("refresh");
        when(jwtTokenProvider.getAccessTokenValiditySeconds()).thenReturn(3600L);

        AuthTokensResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        assertThat(response.getRefreshToken()).isEqualTo("refresh");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
        verify(metricsService).recordUserLogin();
    }

    @Test
    void loginThrowsWhenUserInactive() {
        LoginRequest request = new LoginRequest();
        request.setUid("user");
        request.setPassword("password");

        User inactive = createUser("user", false);
        when(userRepository.findByUid("user")).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void loginThrowsWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUid("ghost");
        request.setPassword("pw");
        when(userRepository.findByUid("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void loginThrowsWhenPasswordMismatch() {
        LoginRequest request = new LoginRequest();
        request.setUid("user");
        request.setPassword("wrong");
        User user = createUser("user", true);
        when(userRepository.findByUid("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void loginHandlesUsersWithoutActiveDevices() {
        LoginRequest request = new LoginRequest();
        request.setUid("user");
        request.setPassword("password");
        User activeUser = createUser("user", true);
        when(userRepository.findByUid("user")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password", activeUser.getPasswordHash())).thenReturn(true);
        when(deviceRepository.findByUser(activeUser)).thenReturn(List.of(createDevice(1L, false)));
        when(jwtTokenProvider.createAccessToken(eq("user"), anyString(), eq(null))).thenReturn("access");
        when(jwtTokenProvider.createRefreshToken("user")).thenReturn("refresh");
        when(jwtTokenProvider.getAccessTokenValiditySeconds()).thenReturn(1000L);

        AuthTokensResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access");
        verify(jwtTokenProvider).createAccessToken("user", activeUser.getRole().name(), null);
    }

    @Test
    void logoutRejectsInvalidToken() {
        when(jwtTokenProvider.validateToken("invalid")).thenReturn(false);

        assertThatThrownBy(() -> authService.logout("invalid"))
                .isInstanceOf(AuthException.class);
        verify(blacklistedTokenRepository, never()).save(any());
    }

    @Test
    void logoutAddsRefreshTokenToBlacklist() {
        String refresh = "refresh-token";
        when(jwtTokenProvider.validateToken(refresh)).thenReturn(true);
        when(blacklistedTokenRepository.existsByToken(refresh)).thenReturn(false);
        when(jwtTokenProvider.getSubjectFromToken(refresh)).thenReturn("user");
        when(jwtTokenProvider.getRefreshTokenValidityMs()).thenReturn(1000L);

        LogoutResponse response = authService.logout(refresh);

        assertThat(response.getMessage()).contains("로그아웃");
        verify(blacklistedTokenRepository).save(any(BlacklistedToken.class));
    }

    @Test
    void refreshThrowsWhenTokenBlacklisted() {
        when(jwtTokenProvider.validateToken("refresh")).thenReturn(true);
        when(blacklistedTokenRepository.existsByToken("refresh")).thenReturn(true);

        assertThatThrownBy(() -> authService.refresh("refresh"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void refreshReissuesTokensAfterValidation() {
        String refresh = "refresh-token";
        User user = createUser("user", true);
        when(jwtTokenProvider.validateToken(refresh)).thenReturn(true);
        when(blacklistedTokenRepository.existsByToken(refresh)).thenReturn(false);
        when(jwtTokenProvider.getSubjectFromToken(refresh)).thenReturn(user.getUid());
        when(userRepository.findByUid(user.getUid())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.getRefreshTokenValidityMs()).thenReturn(2000L);
        when(deviceRepository.findByUser(user)).thenReturn(List.of(createDevice(20L, true)));
        when(jwtTokenProvider.createAccessToken(eq(user.getUid()), anyString(), eq(20L))).thenReturn("access-2");
        when(jwtTokenProvider.createRefreshToken(user.getUid())).thenReturn("refresh-2");
        when(jwtTokenProvider.getAccessTokenValiditySeconds()).thenReturn(7200L);

        RefreshResponse response = authService.refresh(refresh);

        assertThat(response.getAccessToken()).isEqualTo("access-2");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-2");
        assertThat(response.getExpiresIn()).isEqualTo(7200L);
        ArgumentCaptor<BlacklistedToken> tokenCaptor = ArgumentCaptor.forClass(BlacklistedToken.class);
        verify(blacklistedTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getReason()).isEqualTo("TOKEN_REFRESH");
    }

    @Test
    void refreshThrowsWhenUserInactive() {
        String refresh = "refresh";
        User inactive = createUser("user", false);
        when(jwtTokenProvider.validateToken(refresh)).thenReturn(true);
        when(blacklistedTokenRepository.existsByToken(refresh)).thenReturn(false);
        when(jwtTokenProvider.getSubjectFromToken(refresh)).thenReturn(inactive.getUid());
        when(userRepository.findByUid(inactive.getUid())).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> authService.refresh(refresh))
                .isInstanceOf(AuthException.class);
    }

    private User createUser(String uid, boolean active) {
        User user = User.builder()
                .uid(uid)
                .encodedPassword("encoded")
                .phoneE164("+821011112222")
                .name("사용자")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        ReflectionTestUtils.setField(user, "passwordHash", "hashed");
        ReflectionTestUtils.setField(user, "isActive", active);
        return user;
    }

    private Device createDevice(Long id, boolean active) {
        return Device.builder()
                .id(id)
                .deviceUuid("device-" + id)
                .isActive(active)
                .build();
    }
}
