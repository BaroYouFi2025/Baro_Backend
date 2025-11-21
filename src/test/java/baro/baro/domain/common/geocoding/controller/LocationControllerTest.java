package baro.baro.domain.common.geocoding.controller;

import baro.baro.config.JwtAuthenticationFilter;
import baro.baro.domain.common.geocoding.service.GeocodingService;
import baro.baro.domain.common.monitoring.MetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = LocationController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeocodingService geocodingService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private MetricsService metricsService;

    @Test
    void getAddressFromCoordinates_returnsAddressResponse() throws Exception {
        when(geocodingService.getAddressFromCoordinates(37.5, 127.0)).thenReturn("서울 중구 세종대로 110");

        mockMvc.perform(get("/location/address")
                        .param("latitude", "37.5")
                        .param("longitude", "127.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(37.5))
                .andExpect(jsonPath("$.longitude").value(127.0))
                .andExpect(jsonPath("$.address").value("서울 중구 세종대로 110"))
                .andExpect(jsonPath("$.success").value(true));

        verify(geocodingService).getAddressFromCoordinates(eq(37.5), eq(127.0));
    }
}
