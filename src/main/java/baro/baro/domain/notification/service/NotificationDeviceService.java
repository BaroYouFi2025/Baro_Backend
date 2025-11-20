package baro.baro.domain.notification.service;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDeviceService {

    private final DeviceRepository deviceRepository;

    public List<Device> getActiveDevicesWithToken(User user) {
        return deviceRepository.findByUser(user).stream()
                .filter(Device::isActive)
                .filter(device -> StringUtils.hasText(device.getFcmToken()))
                .toList();
    }
}
