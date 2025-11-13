package baro.baro.domain.user.exception;

import lombok.Getter;


@Getter
public enum UserErrorCode {
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(409, "이미 존재하는 사용자입니다."),
    PHONE_ALREADY_EXISTS(409, "이미 등록된 전화번호입니다."),
    INVALID_PASSWORD(400, "비밀번호가 일치하지 않습니다."),
    USER_ALREADY_INACTIVE(400, "이미 비활성화된 사용자입니다."),
    USER_ACCESS_DENIED(403, "사용자 접근이 거부되었습니다.");

    private final String message;
    private final int status;

    UserErrorCode(int status, String message) {
        this.message = message;
        this.status = status;
    }
}