package baro.baro.domain.common.exception;

import java.time.LocalDateTime;

public record ApiErrorResponse(String code, String message, int status, LocalDateTime timestamp) {
    // 편의 메서드
    public static ApiErrorResponse of(ErrorCode errorCode) {
        return new ApiErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                errorCode.getStatus(),
                LocalDateTime.now()
        );
    }

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(
                code,
                message,
                400,
                LocalDateTime.now()
        );
    }
}
