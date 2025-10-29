package baro.baro.domain.ai.exception;

/**
 * AI API Quota 초과 예외
 *
 * <p>Google Gemini API 등 외부 AI 서비스의 할당량(Quota)이 초과되었을 때 발생하는 예외입니다.</p>
 *
 * <p><b>주요 발생 상황:</b></p>
 * <ul>
 *   <li>일일 요청 한도 초과 (Daily quota exceeded)</li>
 *   <li>분당 요청 한도 초과 (Rate limit exceeded)</li>
 *   <li>토큰 사용량 한도 초과 (Token limit exceeded)</li>
 * </ul>
 *
 * <p><b>처리 방법:</b></p>
 * <ul>
 *   <li>재시도 로직 (Exponential backoff)</li>
 *   <li>Fallback 이미지 반환</li>
 *   <li>사용자에게 일시적 오류 안내</li>
 * </ul>
 */
public class AiQuotaExceededException extends RuntimeException {

    private final String quotaType;
    private final Integer retryAfterSeconds;

    public AiQuotaExceededException(String message) {
        super(message);
        this.quotaType = "UNKNOWN";
        this.retryAfterSeconds = null;
    }

    public AiQuotaExceededException(String message, String quotaType) {
        super(message);
        this.quotaType = quotaType;
        this.retryAfterSeconds = null;
    }

    public AiQuotaExceededException(String message, String quotaType, Integer retryAfterSeconds) {
        super(message);
        this.quotaType = quotaType;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getQuotaType() {
        return quotaType;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
