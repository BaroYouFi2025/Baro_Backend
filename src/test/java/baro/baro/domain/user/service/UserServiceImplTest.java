package baro.baro.domain.user.service;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.entity.PhoneVerification;
import baro.baro.domain.auth.exception.PhoneVerificationException;
import baro.baro.domain.auth.repository.PhoneVerificationRepository;
import baro.baro.domain.auth.service.JwtTokenProvider;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.common.util.PhoneNumberUtil;
import baro.baro.domain.device.repository.GpsTrackRepository;
import baro.baro.domain.user.dto.req.DeleteUserRequest;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.dto.req.UpdateProfileRequest;
import baro.baro.domain.user.dto.req.UserSearchRequest;
import baro.baro.domain.user.dto.res.DeleteUserResponse;
import baro.baro.domain.user.dto.res.UserProfileResponse;
import baro.baro.domain.user.dto.res.UserPublicProfileResponse;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PhoneVerificationRepository phoneVerificationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private GpsTrackRepository gpsTrackRepository;
    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "cookieSecure", true);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_successfullyPersistsNewUser() {
        when(userRepository.findByUid("new-user")).thenReturn(Optional.empty());
        when(phoneVerificationRepository.findByPhoneNumber("01012345678"))
                .thenReturn(Optional.of(mock(PhoneVerification.class)));
        when(userRepository.findByPhoneE164("+821012345678")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123!")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<PhoneNumberUtil> mockedStatic = mockStatic(PhoneNumberUtil.class)) {
            mockedStatic.when(() -> PhoneNumberUtil.toE164Format("01012345678")).thenReturn("+821012345678");

            User created = userService.createUser("new-user", "password123!", "01012345678", "Tester", "2000-01-01");

            assertThat(created.getUid()).isEqualTo("new-user");
            assertThat(created.getPhoneE164()).isEqualTo("+821012345678");
            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void createUser_whenUidExists_throwsUserException() {
        when(userRepository.findByUid("dup")).thenReturn(Optional.of(sampleUser("dup")));

        assertThatThrownBy(() -> userService.createUser("dup", "secret", "01000000000", "Tester", "2001-01-01"))
                .isInstanceOf(UserException.class);
    }

    @Test
    void createUser_whenPhoneNotVerified_throwsPhoneVerificationException() {
        when(userRepository.findByUid("new-user")).thenReturn(Optional.empty());
        when(phoneVerificationRepository.findByPhoneNumber("01099999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser("new-user", "secret", "01099999999", "Tester", "2001-01-01"))
                .isInstanceOf(PhoneVerificationException.class);
    }

    @Test
    void signup_generatesTokensAndSetsRefreshCookie() {
        SignupRequest request = signupRequest();
        MockHttpServletResponse httpResponse = new MockHttpServletResponse();

        when(userRepository.findByUid(request.getUid())).thenReturn(Optional.empty());
        when(phoneVerificationRepository.findByPhoneNumber("01012345678"))
                .thenReturn(Optional.of(mock(PhoneVerification.class)));
        when(userRepository.findByPhoneE164("+821012345678")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.createAccessToken(anyString(), anyString(), any())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(request.getUid())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenValiditySeconds()).thenReturn(3600L);

        try (MockedStatic<PhoneNumberUtil> mockedStatic = mockStatic(PhoneNumberUtil.class)) {
            mockedStatic.when(() -> PhoneNumberUtil.toE164Format("01012345678")).thenReturn("+821012345678");

            AuthTokensResponse tokens = userService.signup(request, httpResponse);

            assertThat(tokens.getAccessToken()).isEqualTo("access-token");
            assertThat(tokens.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(tokens.getExpiresIn()).isEqualTo(3600L);

            Cookie[] cookies = httpResponse.getCookies();
            assertThat(cookies).hasSize(1);
            Cookie refreshCookie = cookies[0];
            assertThat(refreshCookie.getName()).isEqualTo("refreshToken");
            assertThat(refreshCookie.isHttpOnly()).isTrue();
            assertThat(refreshCookie.getSecure()).isTrue();
            assertThat(refreshCookie.getAttribute("SameSite")).isEqualTo("Strict");
            verify(metricsService).recordUserRegistration();
        }
    }

    @Test
    void getProfile_returnsCurrentUserProfile() {
        User currentUser = sampleUser("current");
        ReflectionTestUtils.setField(currentUser, "id", 15L);
        currentUser.updateProfile("Current User", "https://img", "#000000", "Pro");
        when(userRepository.findByUid("current")).thenReturn(Optional.of(currentUser));
        setAuthentication(currentUser);

        UserProfileResponse response = userService.getProfile();

        assertThat(response.getUserId()).isEqualTo(15L);
        assertThat(response.getName()).isEqualTo("Current User");
        assertThat(response.getTitle()).isEqualTo("Pro");
    }

    @Test
    void updateProfile_updatesMutableFields() {
        User user = sampleUser("user-1");
        setAuthentication(user);
        when(userRepository.findByUid("user-1")).thenReturn(Optional.of(user));
        UpdateProfileRequest request = UpdateProfileRequest.create("Renamed", "Explorer", "https://profiles/new", "#FFFFFF");

        UserProfileResponse updated = userService.updateProfile(request);

        assertThat(updated.getName()).isEqualTo("Renamed");
        assertThat(updated.getTitle()).isEqualTo("Explorer");
        assertThat(user.getProfileUrl()).isEqualTo("https://profiles/new");
        assertThat(user.getProfileBackgroundColor()).isEqualTo("#FFFFFF");
    }

    @Test
    void deleteUser_deactivatesAccountAfterPasswordMatch() {
        User user = sampleUser("current");
        setAuthentication(user);
        when(userRepository.findByUid("current")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret!", user.getPasswordHash())).thenReturn(true);
        DeleteUserRequest request = DeleteUserRequest.create("secret!");

        DeleteUserResponse response = userService.deleteUser(request);

        assertThat(response.getMessage()).isNotBlank();
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void searchUsers_withUidDelegatesToRepository() {
        UserSearchRequest request = UserSearchRequest.builder()
                .uid("target")
                .page(0)
                .size(2)
                .build();
        Pageable pageable = PageRequest.of(0, 2);
        Slice<User> users = new SliceImpl<>(List.of(sampleUser("target-uid")), pageable, false);
        when(userRepository.findByUidContainingAndIsActiveTrue("target", pageable)).thenReturn(users);

        Slice<UserPublicProfileResponse> result = userService.searchUsers(request);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUid()).isEqualTo("target-uid");
    }

    @Test
    void searchUsers_withoutUidFallsBackToNearbySearch() {
        User currentUser = sampleUser("current");
        setAuthentication(currentUser);
        when(userRepository.findByUid("current")).thenReturn(Optional.of(currentUser));
        when(gpsTrackRepository.findLatestByUser(currentUser)).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 1);
        Slice<User> fallbackUsers = new SliceImpl<>(List.of(sampleUser("neighbor")), pageable, false);
        when(userRepository.findAllByIsActiveTrue(pageable)).thenReturn(fallbackUsers);

        UserSearchRequest request = UserSearchRequest.builder()
                .uid(null)
                .page(0)
                .size(1)
                .build();

        Slice<UserPublicProfileResponse> result = userService.searchUsers(request);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUid()).isEqualTo("neighbor");
    }

    private SignupRequest signupRequest() {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "uid", "new-user");
        ReflectionTestUtils.setField(request, "password", "password123!");
        ReflectionTestUtils.setField(request, "phone", "01012345678");
        ReflectionTestUtils.setField(request, "username", "Tester");
        ReflectionTestUtils.setField(request, "birthDate", "2000-01-01");
        return request;
    }

    private User sampleUser(String uid) {
        User user = User.builder()
                .uid(uid)
                .encodedPassword("encoded-" + uid)
                .phoneE164("+8210" + uid.hashCode())
                .name("Name " + uid)
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        return user;
    }

    private void setAuthentication(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, user.getPasswordHash(), List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
