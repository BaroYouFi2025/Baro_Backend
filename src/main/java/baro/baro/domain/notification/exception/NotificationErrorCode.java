package baro.baro.domain.notification.exception;

import lombok.Getter;

@Getter
public enum NotificationErrorCode {
    NOTIFICATION_NOT_FOUND(404, "존재하지 않는 알림입니다"),
    NOTIFICATION_NOT_OWNED_BY_USER(403, "다른 사용자의 알림에 접근할 수 없습니다");

    private final int status;
    private final String message;

    NotificationErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
