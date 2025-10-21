package baro.baro.domain.device.service;

import baro.baro.domain.device.dto.request.DeviceRegisterRequest;
import baro.baro.domain.device.dto.request.GpsUpdateRequest;
import baro.baro.domain.device.dto.response.DeviceResponse;
import baro.baro.domain.device.dto.response.GpsUpdateResponse;
import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.entity.GpsTrack;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.device.repository.GpsTrackRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 기기 관리 및 GPS 위치 추적 서비스 구현 클래스
 *
 * 주요 기능:
 * - 모바일 기기 등록 및 관리
 * - GPS 위치 정보 수집 및 저장
 * - 기기별 배터리 상태 모니터링
 */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final GpsTrackRepository gpsTrackRepository;

    /** PostGIS 공간 데이터 생성을 위한 GeometryFactory */
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * 새로운 기기를 사용자 계정에 등록합니다.
     *
     * @param uid 사용자 고유 ID
     * @param request 기기 등록 요청 정보 (UUID, OS 타입, OS 버전)
     * @return 등록된 기기 정보
     * @throws IllegalArgumentException 사용자를 찾을 수 없거나 이미 등록된 UUID인 경우
     */
    @Override
    @Transactional
    public DeviceResponse registerDevice(String uid, DeviceRegisterRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. 중복 UUID 확인 - 이미 등록된 UUID인지 검증
        deviceRepository.findByUuid(request.getDeviceUuid())
                .ifPresent(device -> {
                    throw new IllegalArgumentException("Device already registered");
                });

        // 3. 기기 엔티티 생성
        Device device = Device.builder()
                .user(user)
                .uuid(request.getDeviceUuid())
                .osType(request.getOsType())       // OS 타입 (iOS, Android 등)
                .osVersion(request.getOsVersion()) // OS 버전
                .batteryLevel(null)                // 초기 배터리 레벨은 null
                .isActive(true)                    // 등록 시 활성화 상태
                .registeredAt(LocalDateTime.now())
                .build();

        // 4. 데이터베이스에 저장
        Device savedDevice = deviceRepository.save(device);

        // 5. 응답 DTO 생성 및 반환
        return new DeviceResponse(
                savedDevice.getId(),
                savedDevice.getUuid(),
                savedDevice.getBatteryLevel(),
                savedDevice.getOsType(),
                savedDevice.getOsVersion(),
                savedDevice.isActive(),
                savedDevice.getRegisteredAt()
        );
    }

    /**
     * 기기의 GPS 위치 정보를 업데이트하고 추적 이력에 저장합니다.
     *
     * GPS 좌표는 WGS84 좌표계(SRID: 4326)를 사용하며,
     * PostGIS Point 타입으로 데이터베이스에 저장됩니다.
     *
     * @param uid 사용자 고유 ID
     * @param deviceId 기기 ID
     * @param request GPS 위치 정보 (위도, 경도, 배터리 레벨)
     * @return GPS 업데이트 결과
     * @throws IllegalArgumentException 사용자 또는 기기를 찾을 수 없거나 소유권이 없는 경우
     */
    @Override
    @Transactional
    public GpsUpdateResponse updateGps(String uid, Long deviceId, GpsUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. 기기 조회 및 소유권 확인
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new IllegalArgumentException("Device not found or not owned by user"));

        // 3. GPS 위치 Point 객체 생성 (PostGIS 공간 데이터)
        // 주의: Coordinate의 순서는 (경도, 위도) 입니다
        Point location = geometryFactory.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude())
        );
        location.setSRID(4326); // WGS84 좌표계 설정

        // 4. GPS 트랙 엔티티 생성 및 저장
        GpsTrack gpsTrack = GpsTrack.builder()
                .device(device)
                .location(location)
                .recordedAt(LocalDateTime.now())
                .build();

        gpsTrackRepository.save(gpsTrack);

        // 5. 배터리 레벨 업데이트 (선택적)
        if (request.getBatteryLevel() != null) {
            device.updateBatteryLevel(request.getBatteryLevel());
        }

        // 6. 응답 DTO 생성 및 반환
        return new GpsUpdateResponse(
                request.getLatitude(),
                request.getLongitude(),
                gpsTrack.getRecordedAt(),
                "GPS 위치가 업데이트되었습니다."
        );
    }

}
