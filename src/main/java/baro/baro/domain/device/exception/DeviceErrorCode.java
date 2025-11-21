package baro.baro.domain.device.exception;

import lombok.Getter;

@Getter
public enum DeviceErrorCode {
    DEVICE_NOT_FOUND(404, "기기를 찾을 수 없습니다"),
    DEVICE_ALREADY_REGISTERED(400, "이미 등록된 기기입니다"),
    DEVICE_NOT_OWNED_BY_USER(403, "해당 기기에 대한 권한이 없습니다"),
    NEARBY_ALERT_FAILED(500, "주변 실종자 알림 처리 중 오류가 발생했습니다.");

    private final int status;
    private final String message;

    DeviceErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
