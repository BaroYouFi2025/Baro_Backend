package baro.baro.domain.user.exception;

import lombok.Getter;

@Getter
public enum UserErrorCode {
    USER_NOT_FOUND(404,"User not found");

    private final int status;
    private final String message;

    UserErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
