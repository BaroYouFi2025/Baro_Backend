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

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthTokensResponse login(LoginRequest request) {
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

        // 모바일 앱: Access Token과 Refresh Token 모두 응답 본문에 포함
        return new AuthTokensResponse(access, refresh, expiresIn);
    }

    @Override
    public LogoutResponse logout(String refreshToken) {
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

        return new LogoutResponse("로그아웃 되었습니다.");
    }

    @Override
    public RefreshResponse refresh(String refreshToken) {
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

        // 모바일 앱: Access Token과 Refresh Token 모두 응답 본문에 포함
        return new RefreshResponse(newAccessToken, newRefreshToken, expiresIn);
    }
}