package baro.baro.domain.policeoffice.controller;

import baro.baro.domain.policeoffice.dto.req.NearbyPoliceOfficeRequest;
import baro.baro.domain.policeoffice.dto.res.PoliceOfficeResponse;
import baro.baro.domain.policeoffice.service.PoliceOfficeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PoliceOfficeControllerTest {

    @Mock
    private PoliceOfficeService policeOfficeService;

    @InjectMocks
    private PoliceOfficeController policeOfficeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(policeOfficeController).build();
    }

    @Test
    void findNearby_returnsResponsesFromService() throws Exception {
        List<PoliceOfficeResponse> responses = List.of(
                buildResponse(1L, "Ulji Precinct"),
                buildResponse(2L, "Nampo Substation")
        );
        when(policeOfficeService.findNearbyOffices(any())).thenReturn(responses);

        mockMvc.perform(get("/police-offices/nearby")
                        .param("latitude", "37.5665")
                        .param("longitude", "126.9780")
                        .param("radiusMeters", "2000")
                        .param("limit", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].officeName").value("Ulji Precinct"))
                .andExpect(jsonPath("$[1].id").value(2L));

        ArgumentCaptor<NearbyPoliceOfficeRequest> captor = ArgumentCaptor.forClass(NearbyPoliceOfficeRequest.class);
        verify(policeOfficeService).findNearbyOffices(captor.capture());
        NearbyPoliceOfficeRequest captured = captor.getValue();
        assertThat(captured.getLatitude()).isEqualTo(37.5665);
        assertThat(captured.getLongitude()).isEqualTo(126.9780);
        assertThat(captured.getLimit()).isEqualTo(2);
        assertThat(captured.getRadiusMeters()).isEqualTo(2000);
    }

    @Test
    void findNearby_usesDefaultPaginationWhenParamsMissing() throws Exception {
        when(policeOfficeService.findNearbyOffices(any())).thenReturn(List.of());

        mockMvc.perform(get("/police-offices/nearby")
                        .param("latitude", "35.1796")
                        .param("longitude", "129.0756")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        ArgumentCaptor<NearbyPoliceOfficeRequest> captor = ArgumentCaptor.forClass(NearbyPoliceOfficeRequest.class);
        verify(policeOfficeService).findNearbyOffices(captor.capture());
        NearbyPoliceOfficeRequest captured = captor.getValue();
        assertThat(captured.getRadiusMeters()).isEqualTo(NearbyPoliceOfficeRequest.DEFAULT_RADIUS_METERS);
        assertThat(captured.getLimit()).isEqualTo(NearbyPoliceOfficeRequest.DEFAULT_LIMIT);
    }

    private PoliceOfficeResponse buildResponse(Long id, String officeName) {
        return PoliceOfficeResponse.builder()
                .id(id)
                .headquarters("HQ")
                .station("Station")
                .officeName(officeName)
                .officeType("Type")
                .phoneNumber("02-0000-0000")
                .address("Address")
                .latitude(37.0)
                .longitude(127.0)
                .distanceKm(1.2)
                .build();
    }
}
