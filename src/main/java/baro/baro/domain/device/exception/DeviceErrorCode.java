package baro.baro.domain.device.exception;

import lombok.Getter;

@Getter
public enum DeviceErrorCode {
    DEVICE_NOT_FOUND(404, "기기를 찾을 수 없습니다"),
    DEVICE_ALREADY_REGISTERED(400, "이미 등록된 기기입니다"),
    DEVICE_NOT_OWNED_BY_USER(403, "해당 기기에 대한 권한이 없습니다");

    private final int status;
    private final String message;

    DeviceErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
