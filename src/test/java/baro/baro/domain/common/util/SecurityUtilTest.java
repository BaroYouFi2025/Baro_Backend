package baro.baro.domain.common.util;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import baro.baro.domain.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityUtilTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .uid("user-123")
                .encodedPassword("hashed")
                .phoneE164("+821012345678")
                .name("Tester")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_returnsPrincipalUser_whenAuthenticated() {
        UsernamePasswordAuthenticationToken authentication = authenticatedToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUser = SecurityUtil.getCurrentUser();

        assertThat(currentUser).isSameAs(user);
    }

    @Test
    void getCurrentUser_whenAuthenticationMissing_throwsBusinessException() {
        BusinessException exception = assertThrows(BusinessException.class, SecurityUtil::getCurrentUser);

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_ERROR);
    }

    @Test
    void getCurrentUser_whenPrincipalIsAnonymous_throwsBusinessException() {
        UsernamePasswordAuthenticationToken authentication =
                authenticatedToken("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        BusinessException exception = assertThrows(BusinessException.class, SecurityUtil::getCurrentUser);

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_ERROR);
    }

    @Test
    void getCurrentUser_whenPrincipalIsNotUser_throwsBusinessException() {
        UsernamePasswordAuthenticationToken authentication =
                authenticatedToken("regularPrincipal");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        BusinessException exception = assertThrows(BusinessException.class, SecurityUtil::getCurrentUser);

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_ERROR);
    }

    @Test
    void getCurrentUserUid_returnsUidOfAuthenticatedPrincipal() {
        UsernamePasswordAuthenticationToken authentication = authenticatedToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String uid = SecurityUtil.getCurrentUserUid();

        assertThat(uid).isEqualTo("user-123");
    }

    @Test
    void isAuthenticated_returnsTrueWhenPrincipalIsUser() {
        UsernamePasswordAuthenticationToken authentication = authenticatedToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(SecurityUtil.isAuthenticated()).isTrue();
    }

    @Test
    void isAuthenticated_returnsFalseWhenAuthenticationMissing() {
        assertThat(SecurityUtil.isAuthenticated()).isFalse();
    }

    @Test
    void isAuthenticated_returnsFalseWhenPrincipalIsAnonymous() {
        UsernamePasswordAuthenticationToken authentication =
                authenticatedToken("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(SecurityUtil.isAuthenticated()).isFalse();
    }

    @Test
    void getCurrentDeviceId_returnsValueFromAuthenticationDetails() {
        UsernamePasswordAuthenticationToken authentication = authenticatedToken(user);
        authentication.setDetails(Map.of("deviceId", 42L));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long deviceId = SecurityUtil.getCurrentDeviceId();

        assertThat(deviceId).isEqualTo(42L);
    }

    @Test
    void getCurrentDeviceId_returnsNullWhenNoDetails() {
        UsernamePasswordAuthenticationToken authentication = authenticatedToken(user);
        authentication.setDetails("non-map-details");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long deviceId = SecurityUtil.getCurrentDeviceId();

        assertThat(deviceId).isNull();
    }

    @Test
    void getCurrentDeviceId_returnsNullWhenNotAuthenticated() {
        UsernamePasswordAuthenticationToken authentication = unauthenticatedToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Long deviceId = SecurityUtil.getCurrentDeviceId();

        assertThat(deviceId).isNull();
    }

    @Test
    void getCurrentRole_returnsValueFromAuthenticationDetails() {
        UsernamePasswordAuthenticationToken authentication = authenticatedToken(user);
        authentication.setDetails(Map.of("role", "ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = SecurityUtil.getCurrentRole();

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void getCurrentRole_returnsNullWhenNotAuthenticated() {
        UsernamePasswordAuthenticationToken authentication = unauthenticatedToken(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = SecurityUtil.getCurrentRole();

        assertThat(role).isNull();
    }

    private UsernamePasswordAuthenticationToken authenticatedToken(Object principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    private UsernamePasswordAuthenticationToken unauthenticatedToken(Object principal) {
        return new UsernamePasswordAuthenticationToken(principal, null);
    }
}
