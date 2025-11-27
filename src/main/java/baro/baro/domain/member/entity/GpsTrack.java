package baro.baro.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "gps_tracks", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GpsTrack {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
    
    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    private String location;
    
    @Column(name = "recorded_at", nullable = false)
    private ZonedDateTime recordedAt;
}