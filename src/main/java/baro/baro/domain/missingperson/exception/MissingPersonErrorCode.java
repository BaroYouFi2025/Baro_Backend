package baro.baro.domain.missingperson.exception;

import lombok.Getter;

@Getter
public enum MissingPersonErrorCode {
    MISSING_PERSON_NOT_FOUND(404, "실종자를 찾을 수 없습니다."),
    INVALID_LOCATION_FORMAT(400, "위치 정보 형식이 올바르지 않습니다."),
    INVALID_COORDINATES(400, "좌표 정보가 올바르지 않습니다."),
    ADDRESS_LOOKUP_FAILED(500, "주소 조회에 실패했습니다."),
    INVALID_DATE_FORMAT(400, "날짜 형식이 올바르지 않습니다."),
    MISSING_REQUIRED_FIELD(400, "필수 입력 항목이 누락되었습니다."),
    INVALID_PAGINATION(400, "페이지 정보가 올바르지 않습니다."),
    MISSING_CASE_NOT_FOUND(404, "실종 케이스를 찾을 수 없습니다."),
    GEOCODING_SERVICE_ERROR(500, "지오코딩 서비스 오류가 발생했습니다."),
    UNAUTHORIZED_ACCESS(403, "해당 실종자 정보에 접근 권한이 없습니다."),
    CASE_ALREADY_CLOSED(400, "이미 종료된 실종 케이스입니다."),
    SIGHTING_REPORT_FAILED(500, "발견 신고 처리 중 오류가 발생했습니다."),
    NO_ACTIVE_CASE_FOUND(404, "활성화된 실종 케이스를 찾을 수 없습니다."),
    DUPLICATE_SIGHTING_REPORT(400, "이미 최근에 해당 실종자를 신고하셨습니다. 잠시 후 다시 시도해주세요.");

    private final int status;
    private final String message;

    MissingPersonErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
