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
            case 409 -> ErrorCode.CONFLICT;
            case 400 -> ErrorCode.BAD_REQUEST;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
