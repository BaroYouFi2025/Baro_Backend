package baro.baro.domain.ai.exception;

import lombok.Getter;

// AI 도메인 에러 코드
// AI 이미지 생성 및 관리와 관련된 모든 에러 코드를 정의
//
// 에러 분류:
// - 400: 잘못된 요청 (필수 값 누락, 잘못된 형식 등)
// - 404: 리소스를 찾을 수 없음
// - 429: 요청 한도 초과
// - 500: 서버 내부 오류 (API 호출 실패, 이미지 저장 실패 등)
// - 503: 서비스 사용 불가
@Getter
public enum AiErrorCode {
    // 400 잘못된 요청
    PHOTO_URL_REQUIRED(400, "실종자의 사진 URL은 필수입니다."),
    INVALID_PROMPT(400, "프롬프트 생성에 필요한 정보가 부족합니다."),
    INVALID_ASSET_TYPE(400, "인상착의 이미지는 생성 시 자동 적용됩니다. 성장/노화 이미지만 선택 가능합니다."),

    // 404 리소스를 찾을 수 없음
    MISSING_PERSON_NOT_FOUND(404, "실종자를 찾을 수 없습니다."),
    AI_ASSET_NOT_FOUND(404, "AI 에셋을 찾을 수 없습니다."),
    IMAGE_FILE_NOT_FOUND(404, "이미지 파일을 찾을 수 없습니다."),

    // 429 요청 한도 초과
    QUOTA_EXCEEDED(429, "API 사용량 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    RATE_LIMIT_EXCEEDED(429, "분당 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
    DAILY_LIMIT_EXCEEDED(429, "일일 요청 한도를 초과했습니다. 내일 다시 시도해주세요."),

    // 500 서버 내부 오류
    IMAGE_BLOCKED_BY_FILTER(500, "AI 안전 필터에 의해 이미지 생성이 차단되었습니다. 다시 시도해주세요."),
    IMAGE_GENERATION_FAILED(500, "이미지 생성에 실패했습니다."),
    INSUFFICIENT_IMAGES_GENERATED(500, "요청한 이미지 개수를 생성하지 못했습니다. 최소 요구 개수를 충족하지 못했습니다."),
    IMAGE_SAVE_FAILED(500, "이미지 저장에 실패했습니다."),
    IMAGE_LOAD_FAILED(500, "이미지 로드에 실패했습니다."),
    API_CALL_FAILED(500, "외부 API 호출에 실패했습니다."),
    GEMINI_API_ERROR(500, "Google Gemini API 오류가 발생했습니다."),
    EMPTY_RESPONSE(500, "AI 응답이 비어있습니다."),
    INVALID_RESPONSE_FORMAT(500, "AI 응답 형식이 올바르지 않습니다."),

    // 503 서비스 사용 불가
    SERVICE_UNAVAILABLE(503, "AI 이미지 생성 서비스를 일시적으로 사용할 수 없습니다.");

    private final int status;
    private final String message;

    AiErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}