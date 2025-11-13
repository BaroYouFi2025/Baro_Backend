package baro.baro.domain.auth.controller;

import baro.baro.domain.auth.dto.res.PhoneVerificationResponse;
import baro.baro.domain.auth.dto.res.PhoneVerifyResponse;
import baro.baro.domain.auth.service.EmailListener;
import baro.baro.domain.auth.service.PhoneVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Phone Verification", description = "전화번호 인증 API")
@RestController
@RequestMapping("/auth/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;
    private final EmailListener emailListener;

    @Operation(summary = "전화번호 인증 토큰 생성", 
               description = "전화번호 인증을 위한 토큰을 생성하고 이메일 리스너를 시작합니다. 생성된 토큰은 문자 인증에 사용됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 생성 성공",
            content = @Content(schema = @Schema(implementation = PhoneVerificationResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/verifications")
    public ResponseEntity<PhoneVerificationResponse> createVerificationToken() {
        String token = phoneVerificationService.createVerificationToken();
        emailListener.startListening();
        return ResponseEntity.ok(new PhoneVerificationResponse(token));
    }

    @Operation(summary = "전화번호 인증 상태 조회", 
               description = "특정 전화번호의 인증 완료 여부를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PhoneVerifyResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (전화번호 형식 오류)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @GetMapping("/verifications")
    public ResponseEntity<PhoneVerifyResponse> getVerificationStatus(
            @Parameter(description = "인증 상태를 조회할 전화번호 (11자리)", example = "01012345678", required = true)
            @RequestParam String phoneNumber) {
        boolean isVerified = phoneVerificationService.isPhoneNumberVerified(phoneNumber);
        return ResponseEntity.ok(new PhoneVerifyResponse(isVerified));
    }

    @Operation(summary = "테스트용 전화번호 인증 (개발 환경 전용)", 
               description = "테스트를 위해 전화번호 인증을 직접 완료합니다. 개발 환경에서만 사용하세요.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "인증 완료",
            content = @Content(schema = @Schema(implementation = PhoneVerifyResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/verifications/test")
    public ResponseEntity<PhoneVerifyResponse> testVerifyPhone(
            @Parameter(description = "인증할 토큰", required = true)
            @RequestParam String token,
            @Parameter(description = "인증할 전화번호 (11자리)", required = true)
            @RequestParam String phoneNumber) {
        phoneVerificationService.authenticateWithToken(token, phoneNumber);
        return ResponseEntity.ok(new PhoneVerifyResponse(true));
    }
}
