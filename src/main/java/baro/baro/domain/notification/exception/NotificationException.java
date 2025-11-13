package baro.baro.domain.notification.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;

public class NotificationException extends BusinessException {

    private final NotificationErrorCode notificationErrorCode;

    public NotificationException(NotificationErrorCode notificationErrorCode) {
        super(convertToErrorCode(notificationErrorCode));
        this.notificationErrorCode = notificationErrorCode;
    }

    private static ErrorCode convertToErrorCode(NotificationErrorCode notificationErrorCode) {
        return switch (notificationErrorCode.getStatus()) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 403 -> ErrorCode.FORBIDDEN;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.VALIDATION_ERROR;
        };
    }
}
