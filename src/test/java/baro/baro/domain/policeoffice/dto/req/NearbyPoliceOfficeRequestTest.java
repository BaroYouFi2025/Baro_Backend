package baro.baro.domain.policeoffice.dto.req;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NearbyPoliceOfficeRequestTest {

    @Test
    void setRadiusMetersUsesDefaultWhenNullOrLessThanOne() {
        NearbyPoliceOfficeRequest request = new NearbyPoliceOfficeRequest();

        request.setRadiusMeters(null);
        assertThat(request.getRadiusMeters()).isEqualTo(NearbyPoliceOfficeRequest.DEFAULT_RADIUS_METERS);

        request.setRadiusMeters(0);
        assertThat(request.getRadiusMeters()).isEqualTo(NearbyPoliceOfficeRequest.DEFAULT_RADIUS_METERS);

        request.setRadiusMeters(1200);
        assertThat(request.getRadiusMeters()).isEqualTo(1200);
    }

    @Test
    void setLimitClampsValuesBetweenDefaultsAndMax() {
        NearbyPoliceOfficeRequest request = new NearbyPoliceOfficeRequest();

        request.setLimit(null);
        assertThat(request.getLimit()).isEqualTo(NearbyPoliceOfficeRequest.DEFAULT_LIMIT);

        request.setLimit(0);
        assertThat(request.getLimit()).isEqualTo(NearbyPoliceOfficeRequest.DEFAULT_LIMIT);

        request.setLimit(100);
        assertThat(request.getLimit()).isEqualTo(NearbyPoliceOfficeRequest.MAX_LIMIT);

        request.setLimit(10);
        assertThat(request.getLimit()).isEqualTo(10);
    }

    @Test
    void settersStoreProvidedCoordinates() {
        NearbyPoliceOfficeRequest request = new NearbyPoliceOfficeRequest();

        request.setLatitude(37.5);
        request.setLongitude(126.9);

        assertThat(request.getLatitude()).isEqualTo(37.5);
        assertThat(request.getLongitude()).isEqualTo(126.9);
    }
}
