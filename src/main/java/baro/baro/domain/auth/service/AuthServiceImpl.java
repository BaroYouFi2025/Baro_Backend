package baro.baro.domain.auth.service;

import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.entity.BlacklistedToken;
import baro.baro.domain.auth.exception.AuthErrorCode;
import baro.baro.domain.auth.exception.AuthException;
import baro.baro.domain.auth.repository.BlacklistedTokenRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.common.monitoring.MetricsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final MetricsService metricsService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Override
    public AuthTokensResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByUid(request.getUid())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        // 비활성화된 사용자 로그인 차단
        if (!user.isActive()) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String access = jwtTokenProvider.createAccessToken(user.getUid());
        String refresh = jwtTokenProvider.createRefreshToken(user.getUid());
        long expiresIn = jwtTokenProvider.getAccessTokenValiditySeconds();

        // Refresh Token을 Cookie에 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refresh);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/auth");
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        response.addCookie(refreshTokenCookie);

        // 메트릭 기록: 로그인 성공
        metricsService.recordUserLogin();

        // Response에서 refreshToken 제거 (Cookie에만 저장)
        return new AuthTokensResponse(access, expiresIn);
    }

    @Override
    public LogoutResponse logout(String refreshToken, HttpServletResponse response) {
        // Refresh token 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 블랙리스트에 이미 등록된 토큰인지 확인
        if (blacklistedTokenRepository.existsByToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 토큰에서 사용자 정보 및 만료 시간 추출
        String uid = jwtTokenProvider.getSubjectFromToken(refreshToken);
        long validityMs = jwtTokenProvider.getRefreshTokenValidityMs();
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(validityMs * 1_000_000);

        // Refresh Token을 블랙리스트에 추가
        BlacklistedToken blacklistedToken = new BlacklistedToken(
                refreshToken,
                expiresAt,
                "LOGOUT",
                uid
        );
        blacklistedTokenRepository.save(blacklistedToken);

        // Refresh Token Cookie 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/auth");
        refreshTokenCookie.setAttribute("SameSite", "Strict");
        refreshTokenCookie.setMaxAge(0); // 즉시 삭제
        response.addCookie(refreshTokenCookie);

        return new LogoutResponse("로그아웃 되었습니다.");
    }

    @Override
    public RefreshResponse refresh(String refreshToken, HttpServletResponse response) {
        // Refresh token 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 블랙리스트에 있는 토큰인지 확인
        if (blacklistedTokenRepository.existsByToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh token에서 사용자 정보 추출
        String uid = jwtTokenProvider.getSubjectFromToken(refreshToken);

        // 사용자 존재 확인
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // 비활성화된 사용자 토큰 재발급 차단
        if (!user.isActive()) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        // 기존 Refresh Token을 블랙리스트에 추가 (토큰 재사용 방지)
        long validityMs = jwtTokenProvider.getRefreshTokenValidityMs();
        LocalDateTime expiresAt = LocalDateTime.now().plusNanos(validityMs * 1_000_000);
        BlacklistedToken oldToken = new BlacklistedToken(
                refreshToken,
                expiresAt,
                "TOKEN_REFRESH",
                uid
        );
        blacklistedTokenRepository.save(oldToken);

        // 새로운 access token과 refresh token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUid());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUid());
        long expiresIn = jwtTokenProvider.getAccessTokenValiditySeconds();

        // 새로운 Refresh Token을 Cookie에 설정
        Cookie newRefreshTokenCookie = new Cookie("refreshToken", newRefreshToken);
        newRefreshTokenCookie.setHttpOnly(true);
        newRefreshTokenCookie.setSecure(cookieSecure);
        newRefreshTokenCookie.setPath("/auth");
        newRefreshTokenCookie.setAttribute("SameSite", "Strict");
        newRefreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        response.addCookie(newRefreshTokenCookie);

        return new RefreshResponse(newAccessToken, expiresIn);
    }
}