package baro.baro.domain.common.geocoding.exception;

import lombok.Getter;

// Geocoding 도메인 에러 코드
// 지오코딩 및 역지오코딩과 관련된 모든 에러 코드를 정의
//
// 에러 분류:
// - 400: 잘못된 요청 (유효하지 않은 좌표, 빈 주소 등)
// - 404: 변환 결과를 찾을 수 없음
// - 502: 외부 API 오류
// - 503: 서비스 사용 불가
@Getter
public enum GeocodingErrorCode {
    // 400 잘못된 요청
    INVALID_COORDINATES(400, "유효하지 않은 좌표입니다. 위도는 -90~90, 경도는 -180~180 범위여야 합니다."),
    NULL_COORDINATES(400, "위도 또는 경도가 null입니다."),
    INVALID_ADDRESS(400, "주소가 null이거나 빈 문자열입니다."),

    // 404 변환 결과를 찾을 수 없음
    ADDRESS_NOT_FOUND(404, "해당 좌표에 대한 주소를 찾을 수 없습니다."),
    COORDINATES_NOT_FOUND(404, "해당 주소에 대한 좌표를 찾을 수 없습니다."),

    // 502 외부 API 오류
    GEOCODING_API_ERROR(502, "Geocoding API 호출 중 오류가 발생했습니다."),

    // 503 서비스 사용 불가
    GEOCODING_SERVICE_UNAVAILABLE(503, "Geocoding 서비스를 일시적으로 사용할 수 없습니다.");

    private final int status;
    private final String message;

    GeocodingErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
