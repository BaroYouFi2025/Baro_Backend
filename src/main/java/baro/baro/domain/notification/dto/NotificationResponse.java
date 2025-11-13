package baro.baro.domain.notification.dto;

import baro.baro.domain.common.entity.Notification;
import baro.baro.domain.common.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 *
 * 사용자의 알림 정보를 반환합니다.
 */
@Schema(description = "알림 응답")
@Data
public class NotificationResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "알림 타입", example = "INVITE_REQUEST", allowableValues = {"INVITE_REQUEST", "FOUND_REPORT", "NEARBY_ALERT"})
    private NotificationType type;

    @Schema(description = "알림 제목", example = "새로운 구성원 초대 요청")
    private String title;

    @Schema(description = "알림 내용", example = "홍길동님이 아들로 초대 요청을 보냈습니다.")
    private String message;

    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;

    @Schema(description = "생성 시간", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "읽은 시간", example = "2025-01-15T11:00:00")
    private LocalDateTime readAt;

    /**
     * Notification 엔티티로부터 NotificationResponse를 생성합니다.
     *
     * @param notification 알림 엔티티
     * @return NotificationResponse
     */
    public static NotificationResponse from(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.id = notification.getId();
        response.type = notification.getType();
        response.title = notification.getTitle();
        response.message = notification.getMessage();
        response.isRead = notification.isRead();
        response.createdAt = notification.getCreatedAt();
        response.readAt = notification.getReadAt();
        return response;
    }
}
