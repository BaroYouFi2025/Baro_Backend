package baro.baro.domain.image.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ImageException extends BusinessException {

    private final ImageErrorCode imageErrorCode;

    public ImageException(ImageErrorCode imageErrorCode) {
        super(convertToErrorCode(imageErrorCode));
        this.imageErrorCode = imageErrorCode;
    }

    private static ErrorCode convertToErrorCode(ImageErrorCode imageErrorCode) {
        return switch (imageErrorCode.getStatus()) {
            case 400 -> ErrorCode.VALIDATION_ERROR;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
