package baro.baro.domain.auth.controller;

import baro.baro.domain.auth.dto.req.LoginRequest;
import baro.baro.domain.auth.dto.req.LogoutRequest;
import baro.baro.domain.auth.dto.req.RefreshRequest;
import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.auth.dto.res.LogoutResponse;
import baro.baro.domain.auth.dto.res.RefreshResponse;
import baro.baro.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자 로그인을 수행합니다. Access Token과 Refresh Token 모두 응답 본문에 포함됩니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "아이디 또는 비밀번호가 올바르지 않습니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "로그인 시도가 너무 많습니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 에러 Internal Server Error",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthTokensResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokensResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(
        summary = "로그아웃",
        description = "사용자 로그아웃을 수행합니다. Refresh Token을 블랙리스트에 추가하여 무효화합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(schema = @Schema(implementation = LogoutResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "입력 값이 잘못되었습니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 리프레시 토큰입니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        LogoutResponse logoutResponse = authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(logoutResponse);
    }

    @Operation(summary = "토큰 갱신", description = "Access Token과 Refresh Token을 갱신합니다. 새로운 토큰들이 응답 본문에 포함됩니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content = @Content(schema = @Schema(implementation = RefreshResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "입력 값이 잘못되었습니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 리프레시 토큰입니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없습니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse refreshResponse = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(refreshResponse);
    }
}
