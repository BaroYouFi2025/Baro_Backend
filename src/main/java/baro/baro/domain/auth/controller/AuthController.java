package baro.baro.domain.auth.controller;

import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.req.LogoutRequest;
import baro.baro.domain.auth.dto.req.RefreshRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.service.AuthService;
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
        AuthTokensResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request, HttpServletResponse response) {
        LogoutResponse logoutResponse = authService.logout(request, response);
        return ResponseEntity.ok(logoutResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }
}
