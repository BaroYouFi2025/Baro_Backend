package baro.baro.domain.auth.exception;

import lombok.Getter;

@Getter
public enum PhoneVerificationErrorCode {
    INVALID_TOKEN(400, "유효하지 않은 인증 토큰입니다."),
    TOKEN_EXPIRED(400, "인증 토큰이 만료되었습니다."),
    TOKEN_MISMATCH(400, "토큰과 전화번호가 일치하지 않습니다."),
    TOKEN_GENERATION_FAILED(500, "인증 토큰 생성에 실패했습니다."),
    PHONE_NUMBER_REQUIRED(400, "전화번호는 필수입니다."),
    VERIFICATION_FAILED(400, "전화번호 인증에 실패했습니다.");

    private final int status;
    private final String message;

    PhoneVerificationErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}