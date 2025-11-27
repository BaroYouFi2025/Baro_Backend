package baro.baro.domain.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String base64Secret;

    @BeforeEach
    void setUp() {
        // 테스트용 256비트 비밀키 생성 (Base64 인코딩)
        String secretKey = "test-secret-key-for-jwt-token-provider-must-be-at-least-256-bits";
        base64Secret = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));

        // Access Token: 1시간, Refresh Token: 14일
        jwtTokenProvider = new JwtTokenProvider(base64Secret, 3600L, 1209600L);
    }

    @Test
    @DisplayName("Access Token을 생성하고 유효성을 검증한다")
    void createAccessToken_andValidate_success() {
        // Given
        String uid = "user001";
        String role = "ROLE_USER";
        Long deviceId = 1L;

        // When
        String accessToken = jwtTokenProvider.createAccessToken(uid, role, deviceId);

        // Then
        assertThat(accessToken).isNotNull();
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
    }

    @Test
    @DisplayName("Refresh Token을 생성하고 유효성을 검증한다")
    void createRefreshToken_andValidate_success() {
        // Given
        String uid = "user001";

        // When
        String refreshToken = jwtTokenProvider.createRefreshToken(uid);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("Access Token에서 subject를 정확히 추출한다")
    void getSubjectFromToken_extractsCorrectly() {
        // Given
        String uid = "user001";
        String token = jwtTokenProvider.createAccessToken(uid, "ROLE_USER", 1L);

        // When
        String extractedSubject = jwtTokenProvider.getSubjectFromToken(token);

        // Then
        assertThat(extractedSubject).isEqualTo(uid);
    }

    @Test
    @DisplayName("Access Token에서 role을 정확히 추출한다")
    void getRoleFromToken_extractsCorrectly() {
        // Given
        String uid = "user001";
        String role = "ROLE_ADMIN";
        String token = jwtTokenProvider.createAccessToken(uid, role, 1L);

        // When
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // Then
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    @DisplayName("Access Token에서 deviceId를 정확히 추출한다")
    void getDeviceIdFromToken_extractsCorrectly() {
        // Given
        String uid = "user001";
        Long deviceId = 123L;
        String token = jwtTokenProvider.createAccessToken(uid, "ROLE_USER", deviceId);

        // When
        Long extractedDeviceId = jwtTokenProvider.getDeviceIdFromToken(token);

        // Then
        assertThat(extractedDeviceId).isEqualTo(deviceId);
    }

    @Test
    @DisplayName("deviceId가 null인 Access Token도 정상 생성된다")
    void createAccessToken_withNullDeviceId_success() {
        // Given
        String uid = "user001";
        String role = "ROLE_USER";

        // When
        String token = jwtTokenProvider.createAccessToken(uid, role, null);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getDeviceIdFromToken(token)).isNull();
    }

    @Test
    @DisplayName("잘못된 서명의 토큰은 검증에 실패한다")
    void validateToken_withInvalidSignature_returnsFalse() {
        // Given
        String invalidToken = Jwts.builder()
                .setSubject("user001")
                .signWith(Keys.hmacShaKeyFor("different-secret-key-256-bits-must-be-longer-than-this".getBytes()))
                .compact();

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 검증에 실패한다")
    void validateToken_withMalformedToken_returnsFalse() {
        // Given
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰은 검증에 실패한다")
    void validateToken_withEmptyToken_returnsFalse() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void validateToken_withExpiredToken_returnsFalse() {
        // Given
        JwtTokenProvider expiredProvider = new JwtTokenProvider(base64Secret, -1L, 3600L);
        String expiredToken = expiredProvider.createAccessToken("user001", "ROLE_USER", 1L);

        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰에서도 subject를 추출할 수 있다")
    void getSubjectFromToken_withExpiredToken_extractsSubject() {
        // Given
        JwtTokenProvider expiredProvider = new JwtTokenProvider(base64Secret, -1L, 3600L);
        String expiredToken = expiredProvider.createAccessToken("user001", "ROLE_USER", 1L);

        // When
        String subject = jwtTokenProvider.getSubjectFromToken(expiredToken);

        // Then
        assertThat(subject).isEqualTo("user001");
    }

    @Test
    @DisplayName("만료된 토큰에서도 role을 추출할 수 있다")
    void getRoleFromToken_withExpiredToken_extractsRole() {
        // Given
        JwtTokenProvider expiredProvider = new JwtTokenProvider(base64Secret, -1L, 3600L);
        String expiredToken = expiredProvider.createAccessToken("user001", "ROLE_USER", 1L);

        // When
        String role = jwtTokenProvider.getRoleFromToken(expiredToken);

        // Then
        assertThat(role).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("만료된 토큰에서도 deviceId를 추출할 수 있다")
    void getDeviceIdFromToken_withExpiredToken_extractsDeviceId() {
        // Given
        JwtTokenProvider expiredProvider = new JwtTokenProvider(base64Secret, -1L, 3600L);
        String expiredToken = expiredProvider.createAccessToken("user001", "ROLE_USER", 99L);

        // When
        Long deviceId = jwtTokenProvider.getDeviceIdFromToken(expiredToken);

        // Then
        assertThat(deviceId).isEqualTo(99L);
    }

    @Test
    @DisplayName("잘못된 서명의 토큰에서 subject 추출 시 예외를 던진다")
    void getSubjectFromToken_withInvalidSignature_throwsException() {
        // Given
        String invalidToken = Jwts.builder()
                .setSubject("user001")
                .signWith(Keys.hmacShaKeyFor("different-secret-key-256-bits-must-be-longer-than-this".getBytes()))
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getSubjectFromToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Access Token 유효 시간(초)을 정확히 반환한다")
    void getAccessTokenValiditySeconds_returnsCorrectValue() {
        // When
        long validitySeconds = jwtTokenProvider.getAccessTokenValiditySeconds();

        // Then
        assertThat(validitySeconds).isEqualTo(3600L);
    }

    @Test
    @DisplayName("Refresh Token 유효 시간(밀리초)을 정확히 반환한다")
    void getRefreshTokenValidityMs_returnsCorrectValue() {
        // When
        long validityMs = jwtTokenProvider.getRefreshTokenValidityMs();

        // Then
        assertThat(validityMs).isEqualTo(1209600000L); // 14일 = 1209600초 = 1209600000밀리초
    }

    @Test
    @DisplayName("서로 다른 사용자에 대해 다른 토큰을 생성한다")
    void createAccessToken_forDifferentUsers_generatesDifferentTokens() {
        // Given
        String token1 = jwtTokenProvider.createAccessToken("user001", "ROLE_USER", 1L);
        String token2 = jwtTokenProvider.createAccessToken("user002", "ROLE_USER", 1L);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getSubjectFromToken(token1)).isEqualTo("user001");
        assertThat(jwtTokenProvider.getSubjectFromToken(token2)).isEqualTo("user002");
    }

    @Test
    @DisplayName("Refresh Token은 role과 deviceId를 포함하지 않는다")
    void createRefreshToken_doesNotIncludeRoleAndDeviceId() {
        // Given
        String refreshToken = jwtTokenProvider.createRefreshToken("user001");

        // When
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);
        Long deviceId = jwtTokenProvider.getDeviceIdFromToken(refreshToken);

        // Then
        assertThat(role).isNull();
        assertThat(deviceId).isNull();
    }
}
