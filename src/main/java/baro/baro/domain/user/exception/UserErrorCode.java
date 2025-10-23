package baro.baro.domain.user.exception;

import lombok.Getter;

@Getter
public enum UserErrorCode {
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS("U002", "이미 존재하는 사용자입니다."),
    PHONE_ALREADY_EXISTS("U003", "이미 등록된 전화번호입니다."),
    INVALID_PASSWORD("U004", "비밀번호가 일치하지 않습니다."),
    USER_ALREADY_INACTIVE("U005", "이미 비활성화된 사용자입니다.");

    private final String code;
    private final String message;

    UserErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * HTTP 상태 코드를 반환합니다.
     * UserErrorCode는 기본적으로 400 Bad Request를 반환합니다.
     *
     * @return HTTP 상태 코드
     */
    public int getStatus() {
        return 400;
    }

    /**
     * 오류 메시지를 반환합니다.
     *
     * @return 오류 메시지
     */
    public String getMessage() {
        return this.message;
    }
}