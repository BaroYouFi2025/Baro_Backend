package baro.baro.domain.common.exception;

public class ExternalApiException extends BusinessException {

    public ExternalApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalApiException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
