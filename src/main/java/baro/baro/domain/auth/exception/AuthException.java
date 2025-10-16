package baro.baro.domain.auth.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;

public class AuthException extends BusinessException {
    
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
