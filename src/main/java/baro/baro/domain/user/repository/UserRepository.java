package baro.baro.domain.user.repository;

import baro.baro.domain.user.entity.User;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUid(String uid);
    Optional<User> findByPhoneE164(String phoneE164);
    
    // 활성 사용자 전체 조회 (Slice)
    Slice<User> findAllByIsActiveTrue(Pageable pageable);
    
    // UID로 검색 (부분 일치, 활성 사용자만)
    Slice<User> findByUidContainingAndIsActiveTrue(String uid, Pageable pageable);

    // UID로 검색 (부분 일치, 활성 사용자만, 본인 제외)
    Slice<User> findByUidContainingAndIsActiveTrueAndIdNot(String uid, Long excludeUserId, Pageable pageable);

    // 활성 사용자 전체 조회 (본인 제외)
    Slice<User> findAllByIsActiveTrueAndIdNot(Long excludeUserId, Pageable pageable);

    // 특정 위치 기준으로 가까운 순서로 활성 사용자 조회
    //
    // @param-latitude 기준 위도
    // @param-longitude 기준 경도
    // @param-pageable 페이징 정보
    // @return 거리순으로 정렬된 사용자 목록
    @Query(value = "SELECT DISTINCT u.* FROM youfi.users u " +
                   "INNER JOIN youfi.devices d ON u.id = d.user_id " +
                   "INNER JOIN youfi.gps_tracks g ON d.id = g.device_id " +
                   "WHERE u.is_active = true " +
                   "AND g.recorded_at = (SELECT MAX(g2.recorded_at) FROM youfi.gps_tracks g2 WHERE g2.device_id = d.id) " +
                   "ORDER BY ST_Distance(g.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) " +
                   "LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<User> findNearbyActiveUsers(@Param("latitude") Double latitude,
                                      @Param("longitude") Double longitude,
                                      @Param("limit") int limit,
                                      @Param("offset") int offset);

    // 특정 위치 기준으로 가까운 순서로 활성 사용자 조회 (본인 제외)
    @Query(value = "SELECT DISTINCT u.* FROM youfi.users u " +
                   "INNER JOIN youfi.devices d ON u.id = d.user_id " +
                   "INNER JOIN youfi.gps_tracks g ON d.id = g.device_id " +
                   "WHERE u.is_active = true " +
                   "AND u.id != :excludeUserId " +
                   "AND g.recorded_at = (SELECT MAX(g2.recorded_at) FROM youfi.gps_tracks g2 WHERE g2.device_id = d.id) " +
                   "ORDER BY ST_Distance(g.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) " +
                   "LIMIT :limit OFFSET :offset",
           nativeQuery = true)
    List<User> findNearbyActiveUsersExcludingSelf(@Param("latitude") Double latitude,
                                                   @Param("longitude") Double longitude,
                                                   @Param("excludeUserId") Long excludeUserId,
                                                   @Param("limit") int limit,
                                                   @Param("offset") int offset);
}
