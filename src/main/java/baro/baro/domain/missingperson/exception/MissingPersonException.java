package baro.baro.domain.missingperson.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MissingPersonException extends BusinessException {
    private final MissingPersonErrorCode missingPersonErrorCode;

    public MissingPersonException(MissingPersonErrorCode errorCode) {
        super(convertToErrorCode(errorCode));
        this.missingPersonErrorCode = errorCode;
    }

    private static ErrorCode convertToErrorCode(MissingPersonErrorCode missingPersonErrorCode) {
        return switch (missingPersonErrorCode.getStatus()) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 403 -> ErrorCode.FORBIDDEN;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
