package baro.baro.domain.missingperson.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;
import baro.baro.domain.user.entity.User;

import java.time.ZonedDateTime;

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
    @JoinColumn(name = "reporter_id")
    private User reporter;
    
    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}