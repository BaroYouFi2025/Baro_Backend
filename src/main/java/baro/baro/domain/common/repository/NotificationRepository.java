package baro.baro.domain.common.repository;

import baro.baro.domain.common.entity.Notification;
import baro.baro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 사용자의 모든 알림을 조회합니다.
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 사용자의 읽지 않은 알림 개수를 조회합니다.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    long countUnreadByUser(@Param("user") User user);
    
    /**
     * 사용자의 특정 타입 알림을 조회합니다.
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, String type);

    /**
     * 사용자의 읽지 않은 알림만 조회합니다.
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
}
