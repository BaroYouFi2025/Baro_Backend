package baro.baro.domain.user.controller;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @Operation(summary = "회원가입", 
               description = "새로운 사용자를 등록하고 JWT 토큰을 발급합니다. 전화번호 인증이 완료된 상태에서 호출해야 합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthTokensResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthTokensResponse tokens = authService.signup(request);
        return ResponseEntity.ok(tokens);
    }
}
