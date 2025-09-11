package baro.baro.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import baro.baro.domain.missingperson.entity.MissingPerson;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_person_id", nullable = false)
    private MissingPerson missingPerson;
    
    @Column(name = "device_uuid", unique = true, nullable = false)
    private UUID deviceUuid;
    
    @Column(name = "battery_level")
    private Short batteryLevel;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "registered_at", nullable = false)
    private ZonedDateTime registeredAt;
}