package baro.baro.domain.common.util;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocationUtilTest {

    @Test
    void createPoint_withValidCoordinates_returnsPointWithLatitudeAndLongitude() {
        Point point = LocationUtil.createPoint(37.5665, 126.9780);

        assertThat(point.getY()).isEqualTo(37.5665);
        assertThat(point.getX()).isEqualTo(126.9780);
        assertThat(point.getSRID()).isEqualTo(4326);
    }

    @Test
    void createPoint_withInvalidLatitude_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> LocationUtil.createPoint(100.0, 127.0));

        assertThat(exception).hasMessage("위도는 -90에서 90 사이여야 합니다.");
    }

    @Test
    void validateCoordinates_withNullValues_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> LocationUtil.validateCoordinates(null, 127.0));

        assertThat(exception).hasMessage("좌표 정보가 올바르지 않습니다.");
    }

    @Test
    void validateCoordinates_withInvalidLongitude_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> LocationUtil.validateCoordinates(37.0, 200.0));

        assertThat(exception).hasMessage("경도는 -180에서 180 사이여야 합니다.");
    }

    @Test
    void validateCoordinates_withBoundaryValues_doesNotThrow() {
        LocationUtil.validateCoordinates(-90.0, 180.0);
        LocationUtil.validateCoordinates(90.0, -180.0);
    }

    @Test
    void getLatitude_returnsLatitudeFromPoint() {
        Point point = LocationUtil.createPoint(35.0, 129.0);

        assertThat(LocationUtil.getLatitude(point)).isEqualTo(35.0);
    }

    @Test
    void getLongitude_returnsLongitudeFromPoint() {
        Point point = LocationUtil.createPoint(35.0, 129.0);

        assertThat(LocationUtil.getLongitude(point)).isEqualTo(129.0);
    }
}
