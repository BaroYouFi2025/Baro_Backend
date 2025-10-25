package baro.baro.domain.device.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

/**
 * GPS 추적(GpsTrack) 엔티티
 *
 * 기기의 GPS 위치 정보를 시간별로 기록합니다.
 * PostGIS의 Geography 타입을 사용하여 지리적 위치 데이터를 저장하며,
 * WGS84 좌표계(SRID: 4326)를 사용합니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "gps_tracks", schema = "youfi")
public class GpsTrack {
    /** GPS 트랙 고유 ID (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** GPS 위치 정보를 전송한 기기 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    /**
     * GPS 위치 정보 (PostGIS Point 타입)
     * WGS84 좌표계(SRID: 4326) 사용
     * 형식: Point(경도, 위도)
     */
    @Schema(hidden = true) // Swagger 문서에서 제외 (JTS Point 타입은 직렬화 불가)
    @Column(name = "location", columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;

    /** 위치 정보가 기록된 시간 */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
