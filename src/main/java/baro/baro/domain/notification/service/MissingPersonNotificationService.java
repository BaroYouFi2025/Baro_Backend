package baro.baro.domain.notification.service;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.notification.entity.NotificationType;
import baro.baro.domain.user.entity.User;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 실종자 관련 알림 서비스
//
// 실종자 발견 신고, NEARBY_ALERT 알림을 처리합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class MissingPersonNotificationService {

    private final DeviceRepository deviceRepository;
    private final FcmDispatchService fcmDispatchService;
    private final NotificationPersistenceService persistenceService;

    // 실종자 발견 신고 푸시 알림을 발송합니다.
    @Transactional
    public void sendMissingPersonFoundNotification(
            Long sightingId,
            User missingPersonOwner,
            String missingPersonName,
            String reporterName,
            String address) {

        String title = "실종자가 발견되었습니다!";
        String message = String.format("실종자 %s님이 발견되었습니다\n\n" +
                "찾은 팀: %s 님\n" +
                "발견 위치: %s",
                missingPersonName,
                reporterName,
                address != null ? address : "위치 정보 없음");

        persistenceService.save(missingPersonOwner, NotificationType.FOUND_REPORT, title, message, sightingId);

        List<Device> devices = getActiveDevicesWithToken(missingPersonOwner);
        if (devices.isEmpty()) {
            log.warn("실종자 등록자 {}의 활성 기기가 없습니다. 앱내 알림만 저장됩니다.", missingPersonOwner.getName());
            return;
        }

        for (Device device : devices) {
            Message fcmMessage = fcmDispatchService.buildMissingPersonFoundMessage(
                    device.getFcmToken(), title, message,
                    missingPersonName, reporterName, address
            );
            fcmDispatchService.dispatch(fcmMessage, "missing_person_found", device.getFcmToken());
        }

        log.info("실종자 발견 신고 알림 발송 완료 - 실종자: {}, 신고자: {}, 등록자: {}",
                missingPersonName, reporterName, missingPersonOwner.getName());
    }

    // GPS 업데이트한 사용자에게 NEARBY_ALERT 푸시 알림을 발송합니다.
    @Transactional
    public void sendNearbyAlertToReporter(User reporter, String missingPersonName,
                                           double distance, Point reporterLocation,
                                           Long missingPersonId) {
        String title = "주변에 실종자가 있습니다!";
        String message = String.format("실종자 %s가 주변 %.0fm 이내에 있습니다. 주의 깊게 살펴봐 주세요.",
                missingPersonName, distance);

        persistenceService.saveWithLocation(reporter, NotificationType.NEARBY_ALERT, title, message,
                                            missingPersonId, reporterLocation);

        List<Device> devices = getActiveDevicesWithToken(reporter);
        if (devices.isEmpty()) {
            log.warn("GPS 업데이트 사용자 {}의 활성 기기가 없습니다. 앱내 알림만 저장됩니다.", reporter.getName());
            return;
        }

        for (Device device : devices) {
            Message fcmMessage = fcmDispatchService.buildNearbyAlertMessage(
                    device.getFcmToken(), title, message,
                    missingPersonName, reporter.getName(), distance, missingPersonId, "reporter"
            );
            fcmDispatchService.dispatch(fcmMessage, "nearby_alert", device.getFcmToken());
        }

        log.info("NEARBY_ALERT 알림 발송 완료 - 발견자: {}, 실종자: {}, 거리: {}m",
                reporter.getName(), missingPersonName, distance);
    }

    private List<Device> getActiveDevicesWithToken(User user) {
        return deviceRepository.findByUser(user).stream()
                .filter(Device::isActive)
                .filter(device -> device.getFcmToken() != null && !device.getFcmToken().isEmpty())
                .toList();
    }
}
