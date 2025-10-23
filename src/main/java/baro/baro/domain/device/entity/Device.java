package baro.baro.domain.device.entity;

import baro.baro.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기기(Device) 엔티티
 *
 * 사용자가 등록한 모바일 기기 정보를 관리합니다.
 * 각 기기는 고유한 UUID로 식별되며, GPS 추적 및 배터리 모니터링을 지원합니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "devices", schema = "youfi")
@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Device {
    /** 기기 고유 ID (Primary Key) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 기기를 소유한 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** 기기 고유 식별자 (클라이언트에서 생성) */
    @Column(name = "device_uuid", unique = true, nullable = false)
    private String deviceUuid;

    /** 배터리 잔량 (0-100) */
    @Column(name = "battery_level", length = 100)
    private Integer batteryLevel;

    /** 운영체제 타입 (iOS, Android 등) */
    @Column(name = "os_type", length = 20)
    private String osType;

    /** 운영체제 버전 */
    @Column(name = "os_version", length = 20)
    private String osVersion;

    /** 활성화 상태 */
    @Column(name = "is_active")
    private boolean isActive;

    /** 등록 시간 */
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    /**
     * 배터리 레벨을 업데이트합니다.
     *
     * @param batteryLevel 새로운 배터리 레벨 (0-100)
     */
    public void updateBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
