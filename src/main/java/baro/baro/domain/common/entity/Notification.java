package baro.baro.domain.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import baro.baro.domain.common.enums.NotificationType;
import baro.baro.domain.user.entity.User;

/**
 * 알림(Notification) 엔티티
 *
 * 사용자에게 전송되는 푸시 알림 정보를 관리합니다.
 * 초대 요청, 발견 신고, 근처 알림 등의 타입을 지원합니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "notifications", schema = "youfi")
@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Notification {
    
    /** 알림 고유 ID (Primary Key) */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림을 받을 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** 알림 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;

    /** 알림 제목 */
    @Column(name = "title", length = 200)
    private String title;

    /** 알림 내용 */
    @Column(name = "message", length = 500)
    private String message;

    /** 읽음 여부 */
    @Column(name = "is_read")
    private boolean isRead;

    /** 생성 시간 */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** 읽은 시간 */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 알림을 읽음 처리합니다.
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
