package baro.baro.domain.device.service;

import baro.baro.domain.device.dto.event.LoginSuccessEvent;
import baro.baro.domain.device.entity.Device;
import baro.baro.domain.device.repository.DeviceRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.exception.UserException;
import baro.baro.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceLoginEventHandlerTest {

    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private UserRepository userRepository;

    private DeviceServiceImpl deviceService;

    @BeforeEach
    void setUp() {
        deviceService = spy(new DeviceServiceImpl(
                deviceRepository,
                userRepository,
                null, null, null, null, null, null
        ));
    }

    @Test
    void handleLoginActivatesInactiveDevice() {
        LoginSuccessEvent event = new LoginSuccessEvent(this, "user001", "device-uuid");
        User user = createUser("user001");
        Device inactiveDevice = spy(createDevice(1L, "device-uuid", false));

        when(userRepository.findByUid("user001")).thenReturn(Optional.of(user));
        when(deviceRepository.findByUserAndDeviceUuid(user, "device-uuid")).thenReturn(Optional.of(inactiveDevice));

        deviceService.handleLogin(event);

        verify(inactiveDevice).reactivate();
        verify(deviceRepository).save(inactiveDevice);
    }

    @Test
    void handleLoginDoesNotModifyActiveDevice() {
        LoginSuccessEvent event = new LoginSuccessEvent(this, "user001", "device-uuid");
        User user = createUser("user001");
        Device activeDevice = spy(createDevice(1L, "device-uuid", true));

        when(userRepository.findByUid("user001")).thenReturn(Optional.of(user));
        when(deviceRepository.findByUserAndDeviceUuid(user, "device-uuid")).thenReturn(Optional.of(activeDevice));

        deviceService.handleLogin(event);

        verify(activeDevice, never()).reactivate();
        verify(deviceRepository, never()).save(activeDevice);
    }

    @Test
    void handleLoginIgnoresNonExistentDevice() {
        LoginSuccessEvent event = new LoginSuccessEvent(this, "user001", "non-existent-uuid");
        User user = createUser("user001");

        when(userRepository.findByUid("user001")).thenReturn(Optional.of(user));
        when(deviceRepository.findByUserAndDeviceUuid(user, "non-existent-uuid")).thenReturn(Optional.empty());

        deviceService.handleLogin(event);

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void handleLoginReturnsEarlyWhenDeviceUuidIsNull() {
        LoginSuccessEvent event = new LoginSuccessEvent(this, "user001", null);

        deviceService.handleLogin(event);

        verify(userRepository, never()).findByUid(any());
        verify(deviceRepository, never()).findByUserAndDeviceUuid(any(), any());
    }

    @Test
    void handleLoginThrowsWhenUserNotFound() {
        LoginSuccessEvent event = new LoginSuccessEvent(this, "ghost-user", "device-uuid");

        when(userRepository.findByUid("ghost-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.handleLogin(event))
                .isInstanceOf(UserException.class);
    }

    private User createUser(String uid) {
        User user = User.builder()
                .uid(uid)
                .encodedPassword("encoded")
                .phoneE164("+821011112222")
                .name("사용자")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Device createDevice(Long id, String uuid, boolean active) {
        Device device = Device.builder()
                .deviceUuid(uuid)
                .isActive(active)
                .build();
        ReflectionTestUtils.setField(device, "id", id);
        return device;
    }
}
