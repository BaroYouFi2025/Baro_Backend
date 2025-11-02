package baro.baro.domain.user.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;

public class UserException extends BusinessException {

    private final UserErrorCode userErrorCode;

    public UserException(UserErrorCode userErrorCode) {
        super(convertToErrorCode(userErrorCode));
        this.userErrorCode = userErrorCode;}


    private static ErrorCode convertToErrorCode(UserErrorCode userErrorCode) {
        return switch (userErrorCode.getStatus()) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 409 -> ErrorCode.CONFLICT;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 403 -> ErrorCode.FORBIDDEN;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
