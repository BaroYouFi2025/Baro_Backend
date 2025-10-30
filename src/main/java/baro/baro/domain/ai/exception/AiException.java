package baro.baro.domain.ai.exception;

import lombok.Getter;

/**
 * AI 도메인 예외
 *
 * <p>AI 이미지 생성 및 관리 중 발생하는 모든 예외를 처리합니다.</p>
 *
 * <p><b>사용 예시:</b></p>
 * <pre>
 * throw new AiException(AiErrorCode.IMAGE_GENERATION_FAILED);
 * throw new AiException(AiErrorCode.MISSING_PERSON_ID_REQUIRED, "추가 상세 정보");
 * </pre>
 *
 * @see AiErrorCode
 */
@Getter
public class AiException extends RuntimeException {

    private final AiErrorCode errorCode;
    private final String detailMessage;

    /**
     * 에러 코드만으로 예외 생성
     *
     * @param errorCode AI 에러 코드
     */
    public AiException(AiErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    /**
     * 에러 코드와 상세 메시지로 예외 생성
     *
     * @param errorCode AI 에러 코드
     * @param detailMessage 추가 상세 정보
     */
    public AiException(AiErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + (detailMessage != null ? " - " + detailMessage : ""));
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    /**
     * 에러 코드와 원인 예외로 예외 생성
     *
     * @param errorCode AI 에러 코드
     * @param cause 원인 예외
     */
    public AiException(AiErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detailMessage = cause.getMessage();
    }

    /**
     * HTTP 상태 코드 반환
     *
     * @return HTTP 상태 코드
     */
    public int getStatus() {
        return errorCode.getStatus();
    }
}
