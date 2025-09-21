package baro.baro.domain.auth.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class EmailException extends BusinessException {

    private final EmailErrorCode emailErrorCode;

    public EmailException(EmailErrorCode emailErrorCode) {
        super(convertToErrorCode(emailErrorCode));
        this.emailErrorCode = emailErrorCode;
    }

    private static ErrorCode convertToErrorCode(EmailErrorCode emailErrorCode) {
        return switch (emailErrorCode.getStatus()) {
            case 400 -> ErrorCode.VALIDATION_ERROR;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}