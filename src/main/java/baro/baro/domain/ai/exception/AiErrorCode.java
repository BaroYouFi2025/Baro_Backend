package baro.baro.domain.ai.exception;

import lombok.Getter;

/**
 * AI 도메인 에러 코드
 *
 * <p>AI 이미지 생성 및 관리와 관련된 모든 에러 코드를 정의합니다.</p>
 *
 * <p><b>에러 분류:</b></p>
 * <ul>
 *   <li>4xx: 클라이언트 요청 에러 (잘못된 입력, 권한 없음 등)</li>
 *   <li>5xx: 서버 에러 (API 호출 실패, 이미지 저장 실패 등)</li>
 * </ul>
 */
@Getter
public enum AiErrorCode {
    // 4xx 클라이언트 에러
    MISSING_PERSON_ID_REQUIRED(400, "실종자 ID는 필수입니다."),
    ASSET_TYPE_REQUIRED(400, "에셋 타입은 필수입니다."),
    INVALID_ASSET_TYPE(400, "유효하지 않은 에셋 타입입니다."),
    IMAGE_URL_REQUIRED(400, "이미지 URL은 필수입니다."),
    INVALID_IMAGE_URL(400, "유효하지 않은 이미지 URL 형식입니다."),
    MISSING_PERSON_NOT_FOUND(404, "실종자를 찾을 수 없습니다."),
    AI_ASSET_NOT_FOUND(404, "AI 에셋을 찾을 수 없습니다."),

    // 5xx 서버 에러
    IMAGE_GENERATION_FAILED(500, "이미지 생성에 실패했습니다."),
    IMAGE_SAVE_FAILED(500, "이미지 저장에 실패했습니다."),
    API_CALL_FAILED(500, "외부 API 호출에 실패했습니다."),
    GEMINI_API_ERROR(500, "Google Gemini API 오류가 발생했습니다."),
    QUOTA_EXCEEDED(429, "API 사용량 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),

    // AI 생성 관련
    INVALID_PROMPT(400, "프롬프트 생성에 필요한 정보가 부족합니다."),
    EMPTY_RESPONSE(500, "AI 응답이 비어있습니다."),
    INVALID_RESPONSE_FORMAT(500, "AI 응답 형식이 올바르지 않습니다.");

    private final int status;
    private final String message;

    AiErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}