package baro.baro.domain.auth.controller;

import baro.baro.domain.auth.dto.req.LoginRequest;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자 로그인을 수행합니다. Refresh Token은 HttpOnly 쿠키로 전달됩니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            headers = @Header(
                name = "Set-Cookie",
                description = "refreshToken=<token>; HttpOnly; Secure; Path=/auth/refresh; SameSite=Strict; Max-Age=1209600",
                schema = @Schema(type = "string")
            ),
            content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthTokensResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 수행합니다. Refresh Token 쿠키를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            headers = @Header(
                name = "Set-Cookie",
                description = "refreshToken=; HttpOnly; Secure; Path=/auth/refresh; Max-Age=0 (쿠키 삭제)",
                schema = @Schema(type = "string")
            ),
            content = @Content(schema = @Schema(implementation = LogoutResponse.class))
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        LogoutResponse logoutResponse = authService.logout(refreshToken, response);
        return ResponseEntity.ok(logoutResponse);
    }

    @Operation(summary = "토큰 갱신", description = "Access Token을 갱신합니다. 새로운 Refresh Token도 쿠키로 전달됩니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            headers = @Header(
                name = "Set-Cookie",
                description = "refreshToken=<new_token>; HttpOnly; Secure; Path=/auth/refresh; SameSite=Strict; Max-Age=1209600",
                schema = @Schema(type = "string")
            ),
            content = @Content(schema = @Schema(implementation = RefreshResponse.class))
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        RefreshResponse refreshResponse = authService.refresh(refreshToken, response);
        return ResponseEntity.ok(refreshResponse);
    }
}
