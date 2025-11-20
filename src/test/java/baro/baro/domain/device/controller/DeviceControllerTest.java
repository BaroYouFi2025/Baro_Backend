package baro.baro.domain.device.controller;

import baro.baro.config.JwtAuthenticationFilter;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.device.dto.res.DeviceResponse;
import baro.baro.domain.device.dto.res.GpsUpdateResponse;
import baro.baro.domain.device.service.DeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = DeviceController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private MetricsService metricsService;

    @Test
    @DisplayName("기기 등록 성공 시 200과 기기 정보를 반환한다")
    void registerDevice_success_returns200() throws Exception {
        // Given
        String deviceUuid = UUID.randomUUID().toString();
        DeviceResponse response = new DeviceResponse(
                1L,
                deviceUuid,
                85,
                "Android",
                "14.0",
                true,
                java.time.LocalDateTime.now(),
                "token-12345"
        );
        when(deviceService.registerDevice(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceUuid", deviceUuid,
                                "osType", "Android",
                                "model", "Samsung Galaxy S21",
                                "fcmToken", "token-12345"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(1L))
                .andExpect(jsonPath("$.deviceUuid").value(deviceUuid))
                .andExpect(jsonPath("$.osType").value("Android"))
                .andExpect(jsonPath("$.fcmToken").value("token-12345"));

        verify(deviceService).registerDevice(any());
    }

    @Test
    @DisplayName("GPS 위치 업데이트 성공 시 200과 응답을 반환한다")
    void updateGps_success_returns200() throws Exception {
        // Given
        String deviceUuid = UUID.randomUUID().toString();
        GpsUpdateResponse response = new GpsUpdateResponse(
                37.5665,
                126.9780,
                java.time.LocalDateTime.now(),
                "GPS 위치가 업데이트되었습니다."
        );
        when(deviceService.updateGps(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/devices/gps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "deviceUuid", deviceUuid,
                                "latitude", 37.5665,
                                "longitude", 126.9780,
                                "battery", 50
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("GPS 위치가 업데이트되었습니다."))
                .andExpect(jsonPath("$.latitude").value(37.5665))
                .andExpect(jsonPath("$.longitude").value(126.9780));

        verify(deviceService).updateGps(any());
    }
}
