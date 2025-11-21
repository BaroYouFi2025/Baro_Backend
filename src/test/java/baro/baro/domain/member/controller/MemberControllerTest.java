package baro.baro.domain.member.controller;

import baro.baro.config.JwtAuthenticationFilter;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.member.dto.res.LocationInfo;
import baro.baro.domain.member.dto.res.MemberLocationResponse;
import baro.baro.domain.member.service.MemberLocationEmitterRegistry;
import baro.baro.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = MemberController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private MemberLocationEmitterRegistry emitterRegistry;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private MetricsService metricsService;

    @Test
    @DisplayName("구성원 위치 조회 성공 시 200과 위치 목록을 반환한다")
    void getMemberLocations_success_returns200() throws Exception {
        // Given
        MemberLocationResponse.LocationInfo location = MemberLocationResponse.LocationInfo.create(37.5665, 126.9780);
        MemberLocationResponse member1 = MemberLocationResponse.create(
                1L,
                "Member One",
                "가족",
                85,
                1.5,
                location
        );

        MemberLocationResponse.LocationInfo location2 = MemberLocationResponse.LocationInfo.create(37.5700, 126.9800);
        MemberLocationResponse member2 = MemberLocationResponse.create(
                2L,
                "Member Two",
                "친구",
                75,
                2.3,
                location2
        );

        List<MemberLocationResponse> responses = List.of(member1, member2);
        when(memberService.getMemberLocations()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].name").value("Member One"))
                .andExpect(jsonPath("$[0].relationship").value("가족"))
                .andExpect(jsonPath("$[0].batteryLevel").value(85))
                .andExpect(jsonPath("$[0].distance").value(1.5))
                .andExpect(jsonPath("$[0].location.latitude").value(37.5665))
                .andExpect(jsonPath("$[0].location.longitude").value(126.9780))
                .andExpect(jsonPath("$[1].userId").value(2L))
                .andExpect(jsonPath("$[1].name").value("Member Two"));

        verify(memberService).getMemberLocations();
    }

    @Test
    @DisplayName("구성원이 없을 때 빈 배열을 반환한다")
    void getMemberLocations_withNoMembers_returnsEmptyArray() throws Exception {
        // Given
        when(memberService.getMemberLocations()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(memberService).getMemberLocations();
    }

    @Test
    @DisplayName("구성원 위치 조회 시 null 위치 정보도 처리한다")
    void getMemberLocations_withNullLocation_returnsCorrectData() throws Exception {
        // Given
        MemberLocationResponse memberWithoutLocation = MemberLocationResponse.create(
                1L,
                "Member Without Location",
                "가족",
                70,
                null,
                null
        );

        List<MemberLocationResponse> responses = List.of(memberWithoutLocation);
        when(memberService.getMemberLocations()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].name").value("Member Without Location"))
                .andExpect(jsonPath("$[0].batteryLevel").value(70))
                .andExpect(jsonPath("$[0].distance").doesNotExist())
                .andExpect(jsonPath("$[0].location").doesNotExist());

        verify(memberService).getMemberLocations();
    }

    @Test
    @DisplayName("구성원 위치 조회 시 배터리 레벨 경계값을 올바르게 반환한다")
    void getMemberLocations_withBoundaryBatteryLevels_returnsCorrectData() throws Exception {
        // Given
        MemberLocationResponse.LocationInfo location1 = MemberLocationResponse.LocationInfo.create(37.5665, 126.9780);
        MemberLocationResponse memberLowBattery = MemberLocationResponse.create(
                1L,
                "Low Battery",
                "가족",
                0,
                1.0,
                location1
        );

        MemberLocationResponse.LocationInfo location2 = MemberLocationResponse.LocationInfo.create(37.5700, 126.9800);
        MemberLocationResponse memberFullBattery = MemberLocationResponse.create(
                2L,
                "Full Battery",
                "친구",
                100,
                2.0,
                location2
        );

        List<MemberLocationResponse> responses = List.of(memberLowBattery, memberFullBattery);
        when(memberService.getMemberLocations()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].batteryLevel").value(0))
                .andExpect(jsonPath("$[1].batteryLevel").value(100));

        verify(memberService).getMemberLocations();
    }

    @Test
    @DisplayName("구성원 위치 조회 시 다양한 관계 유형을 올바르게 반환한다")
    void getMemberLocations_withVariousRelationships_returnsCorrectData() throws Exception {
        // Given
        MemberLocationResponse.LocationInfo location = MemberLocationResponse.LocationInfo.create(37.5665, 126.9780);
        MemberLocationResponse family = MemberLocationResponse.create(1L, "Family", "가족", 80, 1.0, location);
        MemberLocationResponse friend = MemberLocationResponse.create(2L, "Friend", "친구", 75, 2.0, location);
        MemberLocationResponse colleague = MemberLocationResponse.create(3L, "Colleague", "동료", 90, 3.0, location);
        MemberLocationResponse other = MemberLocationResponse.create(4L, "Other", "기타", 85, 4.0, location);

        List<MemberLocationResponse> responses = List.of(family, friend, colleague, other);
        when(memberService.getMemberLocations()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].relationship").value("가족"))
                .andExpect(jsonPath("$[1].relationship").value("친구"))
                .andExpect(jsonPath("$[2].relationship").value("동료"))
                .andExpect(jsonPath("$[3].relationship").value("기타"));

        verify(memberService).getMemberLocations();
    }

    @Test
    @DisplayName("구성원 위치 조회 시 거리가 0인 경우도 처리한다")
    void getMemberLocations_withZeroDistance_returnsCorrectData() throws Exception {
        // Given
        MemberLocationResponse.LocationInfo location = MemberLocationResponse.LocationInfo.create(37.5665, 126.9780);
        MemberLocationResponse memberSameLocation = MemberLocationResponse.create(
                1L,
                "Same Location",
                "가족",
                85,
                0.0,
                location
        );

        List<MemberLocationResponse> responses = List.of(memberSameLocation);
        when(memberService.getMemberLocations()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distance").value(0.0));

        verify(memberService).getMemberLocations();
    }

    @Test
    @DisplayName("구성원 위치 조회 시 먼 거리도 처리한다")
    void getMemberLocations_withLargeDistance_returnsCorrectData() throws Exception {
        // Given
        MemberLocationResponse.LocationInfo location = MemberLocationResponse.LocationInfo.create(37.5665, 126.9780);
        MemberLocationResponse memberFarAway = MemberLocationResponse.create(
                1L,
                "Far Away",
                "친구",
                60,
                9999.99,
                location
        );

        List<MemberLocationResponse> responses = List.of(memberFarAway);
        when(memberService.getMemberLocations()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distance").value(9999.99));

        verify(memberService).getMemberLocations();
    }

    @Test
    @DisplayName("구성원 위치 조회 시 음수 위도/경도 좌표도 처리한다")
    void getMemberLocations_withNegativeCoordinates_returnsCorrectData() throws Exception {
        // Given
        MemberLocationResponse.LocationInfo location = MemberLocationResponse.LocationInfo.create(-33.8688, -151.2093); // 시드니 근처
        MemberLocationResponse member = MemberLocationResponse.create(
                1L,
                "Southern Hemisphere",
                "가족",
                85,
                1.5,
                location
        );

        List<MemberLocationResponse> responses = List.of(member);
        when(memberService.getMemberLocations()).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location.latitude").value(-33.8688))
                .andExpect(jsonPath("$[0].location.longitude").value(-151.2093));

        verify(memberService).getMemberLocations();
    }

    @Test
    @DisplayName("구성원 위치 조회 시 매우 큰 구성원 목록도 처리한다")
    void getMemberLocations_withLargeList_returnsCorrectData() throws Exception {
        // Given
        java.util.List<MemberLocationResponse> largeList = new java.util.ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            MemberLocationResponse.LocationInfo location = MemberLocationResponse.LocationInfo.create(
                    37.5665 + (i * 0.001),
                    126.9780 + (i * 0.001)
            );
            largeList.add(MemberLocationResponse.create(
                    (long) i,
                    "Member " + i,
                    "가족",
                    80,
                    i * 0.5,
                    location
            ));
        }

        when(memberService.getMemberLocations()).thenReturn(largeList);

        // When & Then
        mockMvc.perform(get("/members/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[99].userId").value(100));

        verify(memberService).getMemberLocations();
    }
}
