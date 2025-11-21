package baro.baro.domain.policeoffice.dto.res;

import baro.baro.domain.policeoffice.entity.PoliceOffice;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import static org.assertj.core.api.Assertions.assertThat;

class PoliceOfficeResponseTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void from_populatesAllFieldsIncludingCoordinates() {
        Point location = geometryFactory.createPoint(new Coordinate(126.9780, 37.5665));
        PoliceOffice office = PoliceOffice.builder()
                .id(1L)
                .headquarters("Seoul HQ")
                .station("Central Station")
                .officeName("Ulji Precinct")
                .officeType("Precinct")
                .phoneNumber("02-1234-5678")
                .address("1 Eulji-ro Jung-gu Seoul")
                .location(location)
                .build();

        PoliceOfficeResponse response = PoliceOfficeResponse.from(office, 1.2);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getHeadquarters()).isEqualTo("Seoul HQ");
        assertThat(response.getStation()).isEqualTo("Central Station");
        assertThat(response.getOfficeName()).isEqualTo("Ulji Precinct");
        assertThat(response.getOfficeType()).isEqualTo("Precinct");
        assertThat(response.getPhoneNumber()).isEqualTo("02-1234-5678");
        assertThat(response.getAddress()).isEqualTo("1 Eulji-ro Jung-gu Seoul");
        assertThat(response.getLatitude()).isEqualTo(37.5665);
        assertThat(response.getLongitude()).isEqualTo(126.9780);
        assertThat(response.getDistanceKm()).isEqualTo(1.2);
    }

    @Test
    void from_returnsNullCoordinatesWhenLocationMissing() {
        PoliceOffice office = PoliceOffice.builder()
                .id(2L)
                .headquarters("Busan HQ")
                .station("Nampo Station")
                .officeName("Nampo Substation")
                .officeType("Substation")
                .phoneNumber("051-111-2222")
                .address("Nampo-dong, Jung-gu Busan")
                .build();

        PoliceOfficeResponse response = PoliceOfficeResponse.from(office, null);

        assertThat(response.getLatitude()).isNull();
        assertThat(response.getLongitude()).isNull();
        assertThat(response.getDistanceKm()).isNull();
    }
}
