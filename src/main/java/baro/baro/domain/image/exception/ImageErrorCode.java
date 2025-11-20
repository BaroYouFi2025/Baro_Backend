package baro.baro.domain.image.exception;

import lombok.Getter;

@Getter
public enum ImageErrorCode {
    EMPTY_FILE(400, "업로드할 파일이 없습니다."),
    INVALID_FILE_TYPE(400, "지원하지 않는 이미지 형식입니다."),
    FILE_TOO_LARGE(400, "파일 크기는 10MB를 초과할 수 없습니다."),
    FILE_SAVE_FAILED(500, "이미지 저장 중 오류가 발생했습니다.");

    private final int status;
    private final String message;

    ImageErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
