package baro.baro.domain.missingperson.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class MissingPersonException extends BusinessException {
    private final MissingPersonErrorCode missingPersonErrorCode;

    public MissingPersonException(MissingPersonErrorCode errorCode) {
        super(ErrorCode.VALIDATION_ERROR);
        this.missingPersonErrorCode = errorCode;
    }
}
