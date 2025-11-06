package baro.baro.domain.missingperson.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import baro.baro.domain.user.entity.User;
import org.locationtech.jts.geom.Point;

import java.time.ZonedDateTime;

/**
 * 실종자 목격/발견 신고 엔티티
 * 
 * 시민이 실종자를 발견했을 때 신고한 정보를 저장합니다.
 */
@Entity
@Table(name = "sightings", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sighting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_case_id", nullable = false)
    private MissingCase missingCase;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    
    /**
     * 발견 위치 (PostGIS Point 타입)
     * WGS84 좌표계(SRID: 4326) 사용
     */
    @Column(name = "location", columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    /**
     * 발견 신고 생성 (Factory Method)
     */
    public static Sighting create(
            MissingCase missingCase,
            User reporter,
            Point location,
            String address,
            String description) {
        
        if (missingCase == null) {
            throw new IllegalArgumentException("실종 케이스는 필수입니다.");
        }
        if (reporter == null) {
            throw new IllegalArgumentException("신고자 정보는 필수입니다.");
        }
        if (location == null) {
            throw new IllegalArgumentException("발견 위치는 필수입니다.");
        }
        
        return Sighting.builder()
                .missingCase(missingCase)
                .reporter(reporter)
                .location(location)
                .address(address)
                .description(description)
                .build();
    }
    
    /**
     * 위도 가져오기
     */
    public Double getLatitude() {
        if (location == null) {
            throw new IllegalStateException("Location is not set");
        }
        return location.getY();
    }
    
    /**
     * 경도 가져오기
     */
    public Double getLongitude() {
        if (location == null) {
            throw new IllegalStateException("Location is not set");
        }
        return location.getX();
    }
}