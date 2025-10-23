package baro.baro.domain.device.repository;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.entity.GpsTrack;
import baro.baro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GpsTrackRepository extends JpaRepository<GpsTrack, Long> {

    /**
     * 특정 기기의 가장 최근 GPS 위치를 조회합니다.
     *
     * @param device 조회할 기기
     * @return 가장 최근 GPS 트랙 정보
     */
    @Query("SELECT g FROM GpsTrack g WHERE g.device = :device ORDER BY g.recordedAt DESC LIMIT 1")
    Optional<GpsTrack> findLatestByDevice(@Param("device") Device device);

    /**
     * 특정 사용자의 모든 기기 중 가장 최근 GPS 위치를 조회합니다.
     *
     * @param user 조회할 사용자
     * @return 가장 최근 GPS 트랙 정보
     */
    @Query("SELECT g FROM GpsTrack g WHERE g.device.user = :user ORDER BY g.recordedAt DESC LIMIT 1")
    Optional<GpsTrack> findLatestByUser(@Param("user") User user);
}
