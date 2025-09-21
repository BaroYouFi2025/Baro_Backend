package baro.baro.domain.auth.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class PhoneVerificationException extends BusinessException {

    private final PhoneVerificationErrorCode phoneErrorCode;

    public PhoneVerificationException(PhoneVerificationErrorCode phoneErrorCode) {
        super(convertToErrorCode(phoneErrorCode));
        this.phoneErrorCode = phoneErrorCode;
    }

    private static ErrorCode convertToErrorCode(PhoneVerificationErrorCode phoneErrorCode) {
        return switch (phoneErrorCode.getStatus()) {
            case 500 -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.VALIDATION_ERROR;
        };
    }
}