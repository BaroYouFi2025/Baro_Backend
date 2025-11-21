package baro.baro.domain.notification.service;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    private NotificationDeviceService notificationDeviceService;

    @BeforeEach
    void setUp() {
        notificationDeviceService = new NotificationDeviceService(deviceRepository);
    }

    @Test
    void returnsOnlyActiveDevicesWithTokens() {
        User user = mock(User.class);
        Device active = Device.builder().isActive(true).fcmToken("token-a").build();
        Device withoutToken = Device.builder().isActive(true).fcmToken("").build();
        Device inactive = Device.builder().isActive(false).fcmToken("token-b").build();
        when(deviceRepository.findByUser(user)).thenReturn(List.of(active, withoutToken, inactive));

        List<Device> result = notificationDeviceService.getActiveDevicesWithToken(user);

        assertThat(result).containsExactly(active);
    }

    @Test
    void returnsEmptyListWhenNoDevicesAvailable() {
        User user = mock(User.class);
        when(deviceRepository.findByUser(user)).thenReturn(List.of());

        List<Device> result = notificationDeviceService.getActiveDevicesWithToken(user);

        assertThat(result).isEmpty();
    }
}
