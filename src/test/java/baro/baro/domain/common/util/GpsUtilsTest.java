package baro.baro.domain.common.util;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.assertj.core.api.Assertions.assertThat;

class GpsUtilsTest {

    @Test
    void calculateDistance_betweenCoordinates_returnsRoundedDistance() {
        double distance = GpsUtils.calculateDistance(37.5665, 126.9780, 35.1796, 129.0756);

        assertThat(distance).isEqualTo(325.11);
    }

    @Test
    void calculateDistance_betweenPoints_returnsRoundedDistance() {
        Point point1 = LocationUtil.createPoint(37.0, 127.0);
        Point point2 = LocationUtil.createPoint(37.5, 127.5);

        double distance = GpsUtils.calculateDistance(point1, point2);

        assertThat(distance).isEqualTo(71.06);
    }

    @Test
    void calculateDistance_whenPointIsNull_returnsZero() {
        Point point = LocationUtil.createPoint(36.0, 128.0);

        assertThat(GpsUtils.calculateDistance(null, point)).isZero();
        assertThat(GpsUtils.calculateDistance(point, null)).isZero();
    }

    @Test
    void getLatitude_returnsLatitudeFromPoint() {
        Point point = LocationUtil.createPoint(34.5, 127.3);

        assertThat(GpsUtils.getLatitude(point)).isEqualTo(34.5);
    }

    @Test
    void getLatitude_whenPointIsNull_returnsNull() {
        assertThat(GpsUtils.getLatitude(null)).isNull();
    }

    @Test
    void getLongitude_returnsLongitudeFromPoint() {
        Point point = LocationUtil.createPoint(34.5, 127.3);

        assertThat(GpsUtils.getLongitude(point)).isEqualTo(127.3);
    }

    @Test
    void getLongitude_whenPointIsNull_returnsNull() {
        assertThat(GpsUtils.getLongitude(null)).isNull();
    }
}
