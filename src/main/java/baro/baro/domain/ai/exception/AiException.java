package baro.baro.domain.ai.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;

// AI 도메인 예외
// AI 이미지 생성 및 관리 중 발생하는 모든 예외를 처리
// 다른 도메인과 동일하게 BusinessException을 상속하여 일관된 예외 처리 구조를 유지
//
// 사용 예시:
// throw new AiException(AiErrorCode.IMAGE_GENERATION_FAILED);
public class AiException extends BusinessException {

    private final AiErrorCode aiErrorCode;

    // 에러 코드로 예외 생성
    public AiException(AiErrorCode aiErrorCode) {
        super(convertToErrorCode(aiErrorCode));
        this.aiErrorCode = aiErrorCode;
    }

    // AI 에러 코드를 공통 ErrorCode로 변환
    private static ErrorCode convertToErrorCode(AiErrorCode aiErrorCode) {
        return switch (aiErrorCode.getStatus()) {
            case 400 -> ErrorCode.BAD_REQUEST;
            case 404 -> ErrorCode.NOT_FOUND;
            case 429 -> ErrorCode.TOO_MANY_REQUESTS;
            case 503 -> ErrorCode.SERVICE_UNAVAILABLE;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

    // AI 에러 코드 반환
    public AiErrorCode getAiErrorCode() {
        return aiErrorCode;
    }
}
