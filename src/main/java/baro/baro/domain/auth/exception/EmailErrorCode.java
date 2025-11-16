package baro.baro.domain.auth.exception;

import lombok.Getter;

@Getter
public enum EmailErrorCode {
    CONNECTION_FAILED(500, "이메일 서버 연결에 실패했습니다."),
    CONNECTION_RETRY_EXCEEDED(500, "이메일 서버 연결 재시도 횟수를 초과했습니다."),
    MAIL_PROCESSING_FAILED(500, "이메일 처리 중 오류가 발생했습니다."),
    MAIL_PARSING_FAILED(400, "이메일 내용 파싱에 실패했습니다."),
    TOKEN_EXTRACTION_FAILED(400, "이메일에서 토큰 추출에 실패했습니다."),
    PHONE_NUMBER_EXTRACTION_FAILED(400, "이메일에서 전화번호 추출에 실패했습니다.");

    private final int status;
    private final String message;

    EmailErrorCode(int status, String message) {
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