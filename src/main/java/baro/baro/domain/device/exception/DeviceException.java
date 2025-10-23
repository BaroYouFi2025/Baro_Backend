package baro.baro.domain.device.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;

public class DeviceException extends BusinessException {

    private final DeviceErrorCode deviceErrorCode;

    public DeviceException(DeviceErrorCode deviceErrorCode) {
        super(convertToErrorCode(deviceErrorCode));
        this.deviceErrorCode = deviceErrorCode;
    }

    private static ErrorCode convertToErrorCode(DeviceErrorCode deviceErrorCode) {
        return switch (deviceErrorCode.getStatus()) {
            case 404 -> ErrorCode.NOT_FOUND;
            case 403 -> ErrorCode.FORBIDDEN;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.VALIDATION_ERROR;
        };
    }
}
