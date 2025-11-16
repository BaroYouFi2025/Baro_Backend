package baro.baro.domain.device.service;


import baro.baro.domain.common.util.GpsUtils;
import baro.baro.domain.device.dto.request.DeviceRegisterRequest;
import baro.baro.domain.device.dto.request.FcmTokenUpdateRequest;
import baro.baro.domain.device.dto.request.GpsUpdateRequest;
import baro.baro.domain.device.dto.response.DeviceResponse;
import baro.baro.domain.device.dto.response.GpsUpdateResponse;
import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.entity.GpsTrack;
import baro.baro.domain.device.exception.DeviceErrorCode;
import baro.baro.domain.device.exception.DeviceException;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.device.repository.GpsTrackRepository;
import baro.baro.domain.missingperson.entity.CaseStatusType;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.repository.MissingCaseRepository;
import baro.baro.domain.missingperson.repository.MissingPersonRepository;
import baro.baro.domain.notification.entity.Notification;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.notification.repository.NotificationRepository;
import baro.baro.domain.notification.service.PushNotificationService;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.exception.UserErrorCode;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.repository.UserRepository;
import baro.baro.domain.common.monitoring.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 기기 관리 및 GPS 위치 추적 서비스 구현 클래스
//
// 주요 기능:
// - 모바일 기기 등록 및 관리
// - GPS 위치 정보 수집 및 저장
// - 기기별 배터리 상태 모니터링
// - 주변 실종자 감지 및 NEARBY_ALERT 알림
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final GpsTrackRepository gpsTrackRepository;
    private final MissingPersonRepository missingPersonRepository;
    private final MissingCaseRepository missingCaseRepository;
    private final NotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;
    private final MetricsService metricsService;

    // PostGIS 공간 데이터 생성을 위한 GeometryFactory
    private final GeometryFactory geometryFactory = new GeometryFactory();

    // NEARBY_ALERT 검색 반경 (미터)
    @Value("${nearby.alert.radius.meters}")
    private int nearbyAlertRadiusMeters;

    // NEARBY_ALERT 쿨타임 (시간)
    @Value("${nearby.alert.cooldown.hours:24}")
    private int nearbyAlertCooldownHours;

    // NEARBY_ALERT 거리 임계값 (미터)
    @Value("${nearby.alert.distance.threshold.meters:500}")
    private double nearbyAlertDistanceThresholdMeters;

    // 새로운 기기를 사용자 계정에 등록합니다.
    //
    // @param uid 사용자 고유 ID
    // @param request 기기 등록 요청 정보 (UUID, OS 타입, OS 버전)
    // @return 등록된 기기 정보
    // @throws UserException 사용자를 찾을 수 없는 경우
    // @throws DeviceException 이미 등록된 UUID인 경우
    @Override
    @Transactional
    public DeviceResponse registerDevice(String uid, DeviceRegisterRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 중복 UUID 확인 - 이미 등록된 UUID인지 검증
        deviceRepository.findByDeviceUuid(request.getDeviceUuid())
                .ifPresent(device -> {
                    throw new DeviceException(DeviceErrorCode.DEVICE_ALREADY_REGISTERED);
                });

        // 3. 기기 엔티티 생성
        Device device = Device.builder()
                .user(user)
                .deviceUuid(request.getDeviceUuid())
                .osType(request.getOsType())       // OS 타입 (iOS, Android 등)
                .osVersion(request.getOsVersion()) // OS 버전
                .fcmToken(request.getFcmToken())   // FCM 토큰
                .batteryLevel(null)                // 초기 배터리 레벨은 null
                .isActive(true)                    // 등록 시 활성화 상태
                .registeredAt(LocalDateTime.now())
                .build();

        // 4. 데이터베이스에 저장
        Device savedDevice = deviceRepository.save(device);

        // 5. 응답 DTO 생성 및 반환
        return new DeviceResponse(
                savedDevice.getId(),
                savedDevice.getDeviceUuid(),
                savedDevice.getBatteryLevel(),
                savedDevice.getOsType(),
                savedDevice.getOsVersion(),
                savedDevice.isActive(),
                savedDevice.getRegisteredAt(),
                savedDevice.getFcmToken()
        );
    }

    // 기기의 GPS 위치 정보를 업데이트하고 추적 이력에 저장합니다.
    //
    // GPS 좌표는 WGS84 좌표계(SRID: 4326)를 사용하며,
    // PostGIS Point 타입으로 데이터베이스에 저장됩니다.
    //
    // @param uid 사용자 고유 ID
    // @param deviceId 기기 ID
    // @param request GPS 위치 정보 (위도, 경도, 배터리 레벨)
    // @return GPS 업데이트 결과
    // @throws UserException 사용자를 찾을 수 없는 경우
    // @throws DeviceException 기기를 찾을 수 없거나 소유권이 없는 경우
    @Override
    @Transactional
    public GpsUpdateResponse updateGps(String uid, Long deviceId, GpsUpdateRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 1. 사용자 조회
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 기기 조회 및 소유권 확인
        Device device = deviceRepository.findByIdAndUser(deviceId, user)
                .orElseThrow(() -> new DeviceException(DeviceErrorCode.DEVICE_NOT_OWNED_BY_USER));

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

        // 6. 비동기로 주변 실종자 체크 및 알림 발송
        checkNearbyMissingPersons(user, location);

        // 7. 메트릭 기록: GPS 업데이트
        metricsService.recordGpsLocationUpdate();
        long duration = System.currentTimeMillis() - startTime;
        metricsService.recordGpsUpdateDuration(duration);

        // 8. 응답 DTO 생성 및 반환
        return new GpsUpdateResponse(
                request.getLatitude(),
                request.getLongitude(),
                gpsTrack.getRecordedAt(),
                "GPS 위치가 업데이트되었습니다."
        );
    }

    // 사용자의 FCM 토큰을 업데이트합니다.
    //
    // @param uid 사용자 고유 ID
    // @param request FCM 토큰 업데이트 요청
    // @throws UserException 사용자를 찾을 수 없는 경우
    // @throws DeviceException 사용자의 활성 기기를 찾을 수 없는 경우
    @Override
    @Transactional
    public void updateFcmToken(String uid, FcmTokenUpdateRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 2. 사용자의 활성 기기 조회 (첫 번째 활성 기기)
        Device device = deviceRepository.findByUser(user).stream()
                .filter(Device::isActive)
                .findFirst()
                .orElseThrow(() -> new DeviceException(DeviceErrorCode.DEVICE_NOT_FOUND));

        // 3. FCM 토큰 업데이트
        device.updateFcmToken(request.getFcmToken());
        deviceRepository.save(device);
    }

    // 주변 실종자를 체크하고 NEARBY_ALERT 알림을 발송합니다.
    // GPS 업데이트 시 비동기로 실행됩니다.
    //
    // @param user GPS 업데이트한 사용자
    // @param location 사용자의 현재 위치
    @Async
    @Transactional
    public void checkNearbyMissingPersons(User user, Point location) {
        try {
            double latitude = location.getY();
            double longitude = location.getX();

            log.debug("주변 실종자 체크 시작 - 사용자: {}, 위치: ({}, {}), 반경: {}m",
                    user.getName(), latitude, longitude, nearbyAlertRadiusMeters);

            // 1. 주변 실종자 검색 (OPEN 케이스만)
            List<MissingPerson> nearbyPersons = missingPersonRepository.findNearbyMissingPersons(
                    latitude,
                    longitude,
                    nearbyAlertRadiusMeters
            );

            if (nearbyPersons.isEmpty()) {
                log.debug("주변에 실종자가 없습니다 - 사용자: {}", user.getName());
                return;
            }

            log.info("주변 실종자 발견 - 사용자: {}, 발견 수: {}", user.getName(), nearbyPersons.size());

            // 2. 각 실종자에 대해 중복 체크 및 알림 발송
            for (MissingPerson missingPerson : nearbyPersons) {
                processMissingPersonAlert(user, missingPerson, location);
            }

        } catch (Exception e) {
            log.error("주변 실종자 체크 중 오류 발생 - 사용자: {}, 오류: {}",
                    user.getName(), e.getMessage(), e);
        }
    }

    // 특정 실종자에 대한 알림 처리 (중복 체크 + 알림 발송)
    //
    // @param user GPS 업데이트한 사용자
    // @param missingPerson 발견된 실종자
    // @param userLocation 사용자 현재 위치
    private void processMissingPersonAlert(User user, MissingPerson missingPerson, Point userLocation) {
        try {
            // 1. 중복 체크: 최근 24시간 이내 알림이 있는지 확인
            if (!shouldSendNearbyAlert(user, missingPerson, userLocation)) {
                log.debug("NEARBY_ALERT 중복 차단 - 사용자: {}, 실종자: {}",
                        user.getName(), missingPerson.getName());
                return;
            }

            // 2. 실종자 등록자 조회 (OPEN 케이스)
            MissingCase missingCase = missingCaseRepository.findByMissingPersonAndCaseStatus(
                    missingPerson,
                    CaseStatusType.OPEN
            ).orElse(null);

            if (missingCase == null) {
                log.warn("OPEN 상태의 케이스를 찾을 수 없음 - 실종자: {}", missingPerson.getName());
                return;
            }

            User owner = missingCase.getReportedBy();

            // 3. 거리 계산
            double distance = GpsUtils.calculateDistance(userLocation, missingPerson.getLocation());

            log.info("NEARBY_ALERT 발송 준비 - 사용자: {}, 실종자: {}, 거리: {}m",
                    user.getName(), missingPerson.getName(), distance);

            // 4. 알림 발송 (등록자 + GPS 업데이트 사용자)
            pushNotificationService.sendNearbyAlertToOwner(
                    owner,
                    user,
                    missingPerson.getName(),
                    distance,
                    userLocation,
                    missingPerson.getId()
            );

            pushNotificationService.sendNearbyAlertToReporter(
                    user,
                    missingPerson.getName(),
                    distance,
                    userLocation,
                    missingPerson.getId()
            );

        } catch (Exception e) {
            log.error("실종자 알림 처리 중 오류 발생 - 사용자: {}, 실종자: {}, 오류: {}",
                    user.getName(), missingPerson.getName(), e.getMessage(), e);
        }
    }

    // NEARBY_ALERT 알림을 발송해야 하는지 판단합니다.
    // 중복 방지 로직: 24시간 + 500m 복합 조건
    //
    // @param user 사용자
    // @param missingPerson 실종자
    // @param currentLocation 현재 위치
    // @return 알림 발송 여부
    private boolean shouldSendNearbyAlert(User user, MissingPerson missingPerson, Point currentLocation) {
        // 1. 최근 24시간 이내의 NEARBY_ALERT 조회
        LocalDateTime threshold = LocalDateTime.now().minusHours(nearbyAlertCooldownHours);
        List<Notification> recentAlerts =
                notificationRepository.findRecentNearbyAlerts(
                        user,
                        missingPerson.getId(),
                        NotificationType.NEARBY_ALERT,
                        threshold
                );

        if (recentAlerts.isEmpty()) {
            return true; // 최근 알림 없음 → 발송 ✅
        }

        // 2. 이전 알림 위치 중 현재 위치에서 500m 이내가 있는지 확인
        for (Notification alert : recentAlerts) {
            if (alert.getRelatedLocation() != null) {
                double distance = GpsUtils.calculateDistance(
                        alert.getRelatedLocation(),
                        currentLocation
                );
                if (distance < nearbyAlertDistanceThresholdMeters) {
                    log.debug("NEARBY_ALERT 중복 - 거리: {}m (임계값: {}m)",
                            distance, nearbyAlertDistanceThresholdMeters);
                    return false; // 500m 이내 이전 알림 있음 → 차단 ❌
                }
            }
        }

        return true; // 모든 이전 알림이 500m 이상 떨어짐 → 발송 ✅
    }

}
