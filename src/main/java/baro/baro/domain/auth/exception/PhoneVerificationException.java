package baro.baro.domain.auth.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;

public class PhoneVerificationException extends BusinessException {

    public PhoneVerificationException(PhoneVerificationErrorCode errorCode) {
        super(ErrorCode.VALIDATION_ERROR);
    }
}