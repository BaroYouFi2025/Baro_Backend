package baro.baro.domain.user.controller;

import baro.baro.domain.auth.dto.res.AuthTokensResponse;
import baro.baro.domain.user.dto.req.SignupRequest;
import baro.baro.domain.user.dto.req.UpdateProfileRequest;
import baro.baro.domain.user.dto.req.DeleteUserRequest;
import baro.baro.domain.user.dto.req.UserSearchRequest;
import baro.baro.domain.user.dto.res.UserProfileResponse;
import baro.baro.domain.user.dto.res.UserPublicProfileResponse;
import baro.baro.domain.user.dto.res.DeleteUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.headers.Header;
import baro.baro.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", 
               description = "새로운 사용자를 등록하고 JWT 토큰을 발급합니다. 전화번호 인증이 완료된 상태에서 호출해야 합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "회원가입 성공",
            headers = @Header(
                name = "Set-Cookie",
                description = "refreshToken=<Token>",
                schema = @Schema(type = "string")
            ),
            content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "요청 데이터가 올바르지 않습니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "UID already exists",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "400", 
            description = "Phone already exists",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "401", 
            description = "전화번호 인증이 필요합니다.",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 에러 Internal Server Error",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthTokensResponse> signup(@Valid @RequestBody SignupRequest request, HttpServletResponse response) {
        AuthTokensResponse tokens = userService.signup(request, response);
        return ResponseEntity.ok(tokens);
    }
    @Operation(
        summary = "사용자 프로필 조회", 
        description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "프로필 조회 성공",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile() {
        UserProfileResponse profile = userService.getProfile();
        return ResponseEntity.ok(profile);
    }

    @Operation(
        summary = "사용자 프로필 수정", 
        description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "프로필 수정 성공",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateProfile(request);
        return ResponseEntity.ok(profile);
    }

    @Operation(
        summary = "사용자 회원 탈퇴", 
        description = "현재 로그인한 사용자의 계정을 비활성화합니다. 비밀번호 확인이 필요합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "회원 탈퇴 성공",
            content = @Content(schema = @Schema(implementation = DeleteUserResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (비밀번호 불일치)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @DeleteMapping("/me")
    public ResponseEntity<DeleteUserResponse> deleteUser(@Valid @RequestBody DeleteUserRequest request) {
        DeleteUserResponse response = userService.deleteUser(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "사용자 검색", 
        description = "UID로 사용자를 검색합니다. UID가 입력되면 해당 사용자를, 입력되지 않으면 사용자 주위 우선으로 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "사용자 검색 성공",
            content = @Content(schema = @Schema(implementation = Slice.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/search")
    public ResponseEntity<Slice<UserPublicProfileResponse>> searchUsers(
            @Parameter(description = "사용자 검색 요청", required = false)
            @RequestBody(required = false) UserSearchRequest request) {
        
        // Request Body가 없으면 기본값으로 생성
        if (request == null) {
            request = UserSearchRequest.builder()
                    .uid(null)
                    .page(0)
                    .size(20)
                    .build();
        }
        
        Slice<UserPublicProfileResponse> users = userService.searchUsers(request);
        return ResponseEntity.ok(users);
    }
}
