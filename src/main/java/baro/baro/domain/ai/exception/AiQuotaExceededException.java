package baro.baro.domain.ai.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;
import lombok.Getter;

// AI API Quota 초과 예외
// Google Gemini API 등 외부 AI 서비스의 할당량(Quota)이 초과되었을 때 발생하는 예외
// 다른 도메인과 동일하게 BusinessException을 상속하여 일관된 예외 처리 구조를 유지
//
// 주요 발생 상황:
// - 일일 요청 한도 초과 (Daily quota exceeded)
// - 분당 요청 한도 초과 (Rate limit exceeded)
// - 토큰 사용량 한도 초과 (Token limit exceeded)
//
// 처리 방법:
// - 재시도 로직 (Exponential backoff)
// - Fallback 이미지 반환
// - 사용자에게 일시적 오류 안내
@Getter
public class AiQuotaExceededException extends BusinessException {

    private final String quotaType;
    private final Integer retryAfterSeconds;

    // 기본 Quota 초과 예외 생성
    public AiQuotaExceededException(String message) {
        super(ErrorCode.TOO_MANY_REQUESTS);
        this.quotaType = "UNKNOWN";
        this.retryAfterSeconds = null;
    }

    // Quota 타입과 함께 예외 생성
    public AiQuotaExceededException(String message, String quotaType) {
        super(ErrorCode.TOO_MANY_REQUESTS);
        this.quotaType = quotaType;
        this.retryAfterSeconds = null;
    }

    // Quota 타입과 재시도 시간과 함께 예외 생성
    public AiQuotaExceededException(String message, String quotaType, Integer retryAfterSeconds) {
        super(ErrorCode.TOO_MANY_REQUESTS);
        this.quotaType = quotaType;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
