package baro.baro.domain.user.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "youfi")
public class Device {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "device_uuid", unique = true, nullable = false)
    private UUID uuid;

    @Column(name = "battery_level", length = 100)
    private Integer batteryLevel;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;
}
