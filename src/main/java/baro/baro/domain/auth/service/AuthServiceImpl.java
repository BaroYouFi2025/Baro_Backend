package baro.baro.domain.auth.service;

import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.req.LogoutRequest;
import baro.baro.domain.auth.dto.req.RefreshRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.exception.AuthException;
import baro.baro.domain.common.exception.ErrorCode;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AuthTokensResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByUid(request.getUserId())
                .orElseThrow(() -> new AuthException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException(ErrorCode.INVALID_CREDENTIALS);
        }

        String access = jwtTokenProvider.createAccessToken(user.getUid());
        String refresh = jwtTokenProvider.createRefreshToken(user.getUid());
        long expiresIn = jwtTokenProvider.getAccessTokenValiditySeconds();

        // Refresh Token을 Cookie에 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refresh);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        response.addCookie(refreshTokenCookie);

        // Response에서 refreshToken 제거 (Cookie에만 저장)
        return new AuthTokensResponse(access, expiresIn);
    }

    @Override
    public LogoutResponse logout(LogoutRequest request, HttpServletResponse response) {
        // Refresh token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh Token Cookie 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 즉시 삭제
        response.addCookie(refreshTokenCookie);

        // 실제 구현에서는 refresh token을 블랙리스트에 추가하거나 DB에서 삭제
        // 현재는 단순히 성공 메시지만 반환
        return new LogoutResponse("로그아웃 되었습니다.");
    }

    @Override
    public RefreshResponse refresh(RefreshRequest request) {
        // Refresh token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Refresh token에서 사용자 정보 추출
        String uid = jwtTokenProvider.getSubjectFromToken(request.getRefreshToken());

        // 사용자 존재 확인
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));

        // 새로운 access token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getUid());
        long expiresIn = jwtTokenProvider.getAccessTokenValiditySeconds();

        return new RefreshResponse(newAccessToken, expiresIn);
    }
}