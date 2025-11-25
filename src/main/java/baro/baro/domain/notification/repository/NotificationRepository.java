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

    // PostGIS 공간 쿼리를 사용한 중복 알림 확인 (성능 개선)
    // 24시간 이내 + 500m 이내 알림이 있는지 한 번의 쿼리로 확인
    //
    // @param user 사용자
    // @param missingPersonId 실종자 ID
    // @param type 알림 타입 (NEARBY_ALERT)
    // @param threshold 시간 임계값
    // @param latitude 현재 위도
    // @param longitude 현재 경도
    // @param distanceMeters 거리 임계값 (미터)
    // @return 중복 알림 존재 여부
    @Query(value = "SELECT EXISTS(" +
           "SELECT 1 FROM youfi.notifications n " +
           "WHERE n.user_id = :userId " +
           "AND n.related_entity_id = :missingPersonId " +
           "AND n.type = CAST(:type AS youfi.notification_type) " +
           "AND n.created_at > :threshold " +
           "AND n.related_location IS NOT NULL " +
           "AND ST_DWithin(" +
           "    n.related_location::geography, " +
           "    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, " +
           "    :distanceMeters" +
           ")" +
           ")", nativeQuery = true)
    boolean existsRecentNearbyAlertWithinDistance(
        @Param("userId") Long userId,
        @Param("missingPersonId") Long missingPersonId,
        @Param("type") String type,
        @Param("threshold") LocalDateTime threshold,
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("distanceMeters") double distanceMeters
    );
}
