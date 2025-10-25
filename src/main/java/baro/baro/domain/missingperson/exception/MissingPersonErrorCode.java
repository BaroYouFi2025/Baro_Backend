package baro.baro.domain.missingperson.exception;

import lombok.Getter;

@Getter
public enum MissingPersonErrorCode {
    MISSING_PERSON_NOT_FOUND("MP001", "실종자를 찾을 수 없습니다."),
    INVALID_LOCATION_FORMAT("MP002", "위치 정보 형식이 올바르지 않습니다."),
    INVALID_COORDINATES("MP003", "좌표 정보가 올바르지 않습니다."),
    ADDRESS_LOOKUP_FAILED("MP004", "주소 조회에 실패했습니다.");

    private final String code;
    private final String message;

    MissingPersonErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
