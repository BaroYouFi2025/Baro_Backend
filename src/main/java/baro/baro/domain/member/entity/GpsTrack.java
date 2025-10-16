package baro.baro.domain.member.entity;

import baro.baro.domain.user.entity.Device;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "gps_tracks")
public class GpsTrack {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Schema(hidden = true) // Swagger 문서에서 제외 (JTS Point 타입은 직렬화 불가)
    @Column(name = "location", columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt; // 위치 기록 시간
}
