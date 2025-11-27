package baro.baro.domain.auth.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends BusinessException {

    private final AuthErrorCode authErrorCode;

    public AuthException(AuthErrorCode authErrorCode) {
        super(convertToErrorCode(authErrorCode));
        this.authErrorCode = authErrorCode;
    }

    private static ErrorCode convertToErrorCode(AuthErrorCode authErrorCode) {
        return switch (authErrorCode.getStatus()) {
            case 401 -> ErrorCode.AUTH_ERROR;
            case 404 -> ErrorCode.NOT_FOUND;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
