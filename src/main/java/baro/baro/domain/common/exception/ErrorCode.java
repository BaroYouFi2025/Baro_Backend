package baro.baro.domain.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    VALIDATION_ERROR(400, "입력 값이 잘못되었습니다."),
    AUTH_ERROR(401, "인증에 실패했습니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다."),
    CONFLICT(409, "이미 존재하는 리소스입니다."),
    INTERNAL_ERROR(500, "서버 내부 오류");

    private final int status;
    // 메시지 설정 메서드
    // 이 메서드는 enum의 불변성을 깨뜨릴 수 있으므로 주의해서 사용해야 합니다.
    private final String message;

    // 생성자
    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

}