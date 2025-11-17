package baro.baro.domain.notification.repository;


import baro.baro.domain.notification.entity.Notification;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 사용자의 모든 알림을 조회합니다.
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // 사용자의 읽지 않은 알림 개수를 조회합니다.
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    long countUnreadByUser(@Param("user") User user);

    // 사용자의 특정 타입 알림을 조회합니다.
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, String type);

    // 사용자의 읽지 않은 알림만 조회합니다.
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    // 최근 시간 내에 특정 사용자가 특정 실종자에 대해 받은 NEARBY_ALERT 알림을 조회합니다.
    // 중복 알림 방지를 위해 사용됩니다.
    //
    // @param user 사용자
    // @param missingPersonId 실종자 ID
    // @param type 알림 타입 (NEARBY_ALERT)
    // @param threshold 시간 임계값 (예: 24시간 전)
    // @return 최근 알림 목록
    @Query("SELECT n FROM Notification n " +
           "WHERE n.user = :user " +
           "AND n.relatedEntityId = :missingPersonId " +
           "AND n.type = :type " +
           "AND n.createdAt > :threshold " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findRecentNearbyAlerts(
        @Param("user") User user,
        @Param("missingPersonId") Long missingPersonId,
        @Param("type") NotificationType type,
        @Param("threshold") LocalDateTime threshold
    );
}
