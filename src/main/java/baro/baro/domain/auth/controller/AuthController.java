package baro.baro.domain.auth.controller;

import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.req.LogoutRequest;
import baro.baro.domain.auth.dto.req.RefreshRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(@RequestBody LoginRequest request) {
        AuthTokensResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestBody LogoutRequest request) {
        LogoutResponse response = authService.logout(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }
}
