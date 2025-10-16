package baro.baro.domain.auth.controller;

import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.req.LogoutRequest;
import baro.baro.domain.auth.dto.req.RefreshRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthTokensResponse authResponse = authService.login(request);
        
        // Refresh Token을 Cookie에 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        response.addCookie(refreshTokenCookie);
        
        // Response에서 refreshToken 제거 (Cookie에만 저장)
        AuthTokensResponse responseBody = new AuthTokensResponse(
            authResponse.getAccessToken(), 
            null, // refreshToken은 Cookie에만 저장
            authResponse.getExpiresIn()
        );
        
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request, HttpServletResponse response) {
        LogoutResponse logoutResponse = authService.logout(request);
        
        // Refresh Token Cookie 삭제
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 즉시 삭제
        response.addCookie(refreshTokenCookie);
        
        return ResponseEntity.ok(logoutResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }
}
