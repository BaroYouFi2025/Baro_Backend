package baro.baro.domain.policeoffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.ZonedDateTime;

/**
 * 경찰관서(지구대/파출소) 정적 데이터 엔티티.
 * CSV에서 가져온 기본 정보와 Google Geocoding API로 채워질 좌표 정보를 포함한다.
 */
@Getter
@Builder
@Entity
@Table(name = "police_offices", schema = "youfi")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PoliceOffice {

    @Id
    private Long id;

    @Column(name = "headquarters", length = 50, nullable = false)
    private String headquarters; // 시도청

    @Column(name = "station", length = 100, nullable = false)
    private String station; // 관할 경찰서

    @Column(name = "office_name", length = 100, nullable = false)
    private String officeName; // 관서명

    @Column(name = "office_type", length = 30, nullable = false)
    private String officeType; // 지구대/파출소 구분

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;

    @Setter
    @Column(name = "location", columnDefinition = "geography(Point,4326)")
    private Point location;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public void updateLocation(Point location) {
        this.location = location;
    }
}
