package baro.baro.domain.common.geocoding.exception;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.exception.ErrorCode;

// Geocoding 도메인 예외
// 지오코딩 및 역지오코딩 중 발생하는 모든 예외를 처리
// 다른 도메인과 동일하게 BusinessException을 상속하여 일관된 예외 처리 구조를 유지
//
// 사용 예시:
// throw new GeocodingException(GeocodingErrorCode.INVALID_COORDINATES);
// throw new GeocodingException(GeocodingErrorCode.GEOCODING_SERVICE_UNAVAILABLE, cause);
public class GeocodingException extends BusinessException {

    private final GeocodingErrorCode geocodingErrorCode;

    // 에러 코드로 예외 생성
    public GeocodingException(GeocodingErrorCode geocodingErrorCode) {
        super(convertToErrorCode(geocodingErrorCode));
        this.geocodingErrorCode = geocodingErrorCode;
    }

    // 에러 코드와 원인 예외로 예외 생성
    public GeocodingException(GeocodingErrorCode geocodingErrorCode, Throwable cause) {
        super(convertToErrorCode(geocodingErrorCode), cause);
        this.geocodingErrorCode = geocodingErrorCode;
    }

    // Geocoding 에러 코드를 공통 ErrorCode로 변환
    private static ErrorCode convertToErrorCode(GeocodingErrorCode geocodingErrorCode) {
        return switch (geocodingErrorCode.getStatus()) {
            case 400 -> ErrorCode.BAD_REQUEST;
            case 404 -> ErrorCode.NOT_FOUND;
            case 502 -> ErrorCode.API_ERROR;
            case 503 -> ErrorCode.SERVICE_UNAVAILABLE;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

    // Geocoding 에러 코드 반환
    public GeocodingErrorCode getGeocodingErrorCode() {
        return geocodingErrorCode;
    }

    // 상세 에러 메시지 반환 (Geocoding 도메인 특화 메시지)
    @Override
    public String getMessage() {
        return geocodingErrorCode.getMessage();
    }
}
