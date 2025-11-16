package baro.baro.domain.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(400, "잘못된 요청입니다."),
    VALIDATION_ERROR(400, "입력 값이 잘못되었습니다."),
    AUTH_ERROR(401, "인증에 실패했습니다."),
    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_EXPIRED(401, "토큰이 만료되었습니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(405, "허용되지 않은 HTTP 메서드입니다."),
    CONFLICT(409, "이미 존재하는 리소스입니다."),
    TOO_MANY_REQUESTS(429, "요청이 너무 많습니다."),
    INTERNAL_ERROR(500, "서버 내부 오류"),
    SERVICE_UNAVAILABLE(503, "서비스를 사용할 수 없습니다.");

    private final int status;
    // 메시지 설정 메서드
    // 이 메서드는 enum의 불변성을 깨뜨릴 수 있으므로 주의해서 사용해야 합니다.
    private final String message;

    // 생성자
    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // HTTP 상태 코드를 반환합니다.
    //
    // @return HTTP 상태 코드
    public int getStatus() {
        return this.status;
    }

    // 오류 메시지를 반환합니다.
    //
    // @return 오류 메시지
    public String getMessage() {
        return this.message;
    }
}