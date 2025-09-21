package baro.baro.domain.auth.controller;

import baro.baro.domain.auth.dto.res.PhoneVerificationResponse;
import baro.baro.domain.auth.dto.res.PhoneVerifyResponse;
import baro.baro.domain.auth.service.EmailListener;
import baro.baro.domain.auth.service.PhoneVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;
    private final EmailListener emailListener;

    /**
     * 전화번호 인증 토큰 생성
     */
    @PostMapping("/verifications")
    public ResponseEntity<PhoneVerificationResponse> createVerificationToken() {
        String token = phoneVerificationService.createVerificationToken();
        emailListener.startListening();
        return ResponseEntity.ok(new PhoneVerificationResponse(token));
    }

    /**
     * 전화번호 인증 상태 조회
     */
    @GetMapping("/verifications")
    public ResponseEntity<PhoneVerifyResponse> getVerificationStatus(
            @RequestParam String phoneNumber) {
        boolean isVerified = phoneVerificationService.isPhoneNumberVerified(phoneNumber);
        return ResponseEntity.ok(new PhoneVerifyResponse(isVerified));
    }
}
