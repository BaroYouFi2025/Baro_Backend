package baro.baro.domain.policeoffice.service;

import baro.baro.domain.common.util.GpsUtils;
import baro.baro.domain.policeoffice.dto.req.NearbyPoliceOfficeRequest;
import baro.baro.domain.policeoffice.dto.res.PoliceOfficeResponse;
import baro.baro.domain.policeoffice.entity.PoliceOffice;
import baro.baro.domain.policeoffice.repository.PoliceOfficeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoliceOfficeServiceImplTest {

    @Mock
    private PoliceOfficeRepository policeOfficeRepository;

    @InjectMocks
    private PoliceOfficeServiceImpl policeOfficeService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void findNearbyOffices_mapsEntitiesToResponsesWithDistance() {
        NearbyPoliceOfficeRequest request = createRequest(37.5665, 126.9780, 3000, 3);
        Point location = geometryFactory.createPoint(new Coordinate(126.9820, 37.5700));
        PoliceOffice office = createOffice(1L, location);
        when(policeOfficeRepository.findNearbyOffices(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusMeters(),
                request.getLimit()
        )).thenReturn(List.of(office));

        List<PoliceOfficeResponse> responses = policeOfficeService.findNearbyOffices(request);

        assertThat(responses).hasSize(1);
        PoliceOfficeResponse response = responses.get(0);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLatitude()).isEqualTo(37.5700);
        assertThat(response.getLongitude()).isEqualTo(126.9820);
        double expectedDistance = GpsUtils.calculateDistance(
                request.getLatitude(),
                request.getLongitude(),
                37.5700,
                126.9820
        );
        assertThat(response.getDistanceKm()).isEqualTo(expectedDistance);

        verify(policeOfficeRepository).findNearbyOffices(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusMeters(),
                request.getLimit()
        );
    }

    @Test
    void findNearbyOffices_handlesEntitiesWithoutLocation() {
        NearbyPoliceOfficeRequest request = createRequest(35.1796, 129.0756, 5000, 5);
        PoliceOffice office = createOffice(2L, null);
        when(policeOfficeRepository.findNearbyOffices(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusMeters(),
                request.getLimit()
        )).thenReturn(List.of(office));

        List<PoliceOfficeResponse> responses = policeOfficeService.findNearbyOffices(request);

        assertThat(responses).hasSize(1);
        PoliceOfficeResponse response = responses.get(0);
        assertThat(response.getLatitude()).isNull();
        assertThat(response.getLongitude()).isNull();
        assertThat(response.getDistanceKm()).isNull();
    }

    private NearbyPoliceOfficeRequest createRequest(double lat, double lon, int radius, int limit) {
        NearbyPoliceOfficeRequest request = new NearbyPoliceOfficeRequest();
        request.setLatitude(lat);
        request.setLongitude(lon);
        request.setRadiusMeters(radius);
        request.setLimit(limit);
        return request;
    }

    private PoliceOffice createOffice(Long id, Point location) {
        return PoliceOffice.builder()
                .id(id)
                .headquarters("HQ")
                .station("Station")
                .officeName("Office " + id)
                .officeType("Type")
                .phoneNumber("02-0000-0000")
                .address("Address " + id)
                .location(location)
                .build();
    }
}
