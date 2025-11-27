package baro.baro.domain.notification.entity;

import baro.baro.domain.notification.exception.NotificationErrorCode;
import baro.baro.domain.notification.exception.NotificationException;
import baro.baro.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

// 알림(Notification) 엔티티
//
// 사용자에게 전송되는 푸시 알림 정보를 관리합니다.
// 초대 요청, 발견 신고, 근처 알림 등의 타입을 지원합니다.
@Entity
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "notifications", schema = "youfi")
@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Notification {

    // 알림 고유 ID (Primary Key)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받을 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 알림 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;

    // 알림 제목
    @Column(name = "title", length = 200)
    private String title;

    // 알림 내용
    @Column(name = "message", length = 500)
    private String message;

    // 읽음 여부
    @Column(name = "is_read")
    private boolean isRead;

    // 생성 시간
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 읽은 시간
    @Column(name = "read_at")
    private LocalDateTime readAt;

    // 관련 엔티티 ID (실종자 ID, 초대 ID 등)
    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    // 알림 관련 위치 (NEARBY_ALERT의 경우 알림 발생 시 사용자 위치)
    @Column(name = "related_location", columnDefinition = "geography(Point,4326)")
    private Point relatedLocation;

    // 알림을 읽음 처리합니다.
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    // 알림 소유자인지 확인합니다.
    public boolean isOwnedBy(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    // 소유권 검증 후 예외를 발생시킵니다.
    public void validateOwnership(Long userId) {
        if (!isOwnedBy(userId)) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_OWNED_BY_USER);
        }
    }

    // 특정 알림 타입인지 확인합니다.
    public boolean isType(NotificationType expectedType) {
        return this.type == expectedType;
    }

    // 알림 타입 검증 후 예외를 발생시킵니다.
    public void validateType(NotificationType expectedType) {
        if (!isType(expectedType)) {
            throw new NotificationException(NotificationErrorCode.INVALID_NOTIFICATION_TYPE);
        }
    }

    // 실종자 관련 알림인지 확인합니다 (FOUND_REPORT, NEARBY_ALERT).
    public boolean isMissingPersonRelated() {
        return this.type == NotificationType.FOUND_REPORT ||
               this.type == NotificationType.NEARBY_ALERT;
    }

    // 실종자 관련 알림인지 검증 후 예외를 발생시킵니다.
    public void validateMissingPersonRelated() {
        if (!isMissingPersonRelated()) {
            throw new NotificationException(NotificationErrorCode.INVALID_NOTIFICATION_TYPE);
        }
    }

    // 관련 엔티티 ID가 존재하는지 확인합니다.
    public boolean hasRelatedEntity() {
        return this.relatedEntityId != null;
    }

    // 관련 엔티티 존재 여부를 검증 후 예외를 발생시킵니다.
    public void validateHasRelatedEntity() {
        if (!hasRelatedEntity()) {
            throw new NotificationException(NotificationErrorCode.RELATED_ENTITY_NOT_FOUND);
        }
    }
}
