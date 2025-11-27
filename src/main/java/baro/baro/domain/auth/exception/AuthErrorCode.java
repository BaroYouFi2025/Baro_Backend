package baro.baro.domain.auth.exception;

import lombok.Getter;

// 인증 도메인 에러 코드
// 로그인, 토큰 검증, 사용자 인증과 관련된 모든 에러 코드를 정의
@Getter
public enum AuthErrorCode {
    // 401 인증 실패
    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 리프레시 토큰입니다."),
    TOKEN_EXPIRED(401, "토큰이 만료되었습니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),

    // 404 사용자를 찾을 수 없음
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다.");

    private final int status;
    private final String message;

    AuthErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}