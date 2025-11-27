package baro.baro.domain.common.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class LocationUtil {
    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    // 위도(latitude), 경도(longitude)로 Point 객체 생성
    // 좌표 유효성을 자동으로 검증합니다.
    public static Point createPoint(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        // 주의: Coordinate(longitude, latitude) 순서!
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    // Point 객체에서 위도(latitude) 추출
    public static double getLatitude(Point point) {
        return point.getY();
    }

    // Point 객체에서 경도(longitude) 추출
    public static double getLongitude(Point point) {
        return point.getX();
    }

    // 좌표 유효성 검증
    // @param latitude 위도 (-90 ~ 90)
    // @param longitude 경도 (-180 ~ 180)
    // @throws IllegalArgumentException 좌표가 유효하지 않은 경우
    public static void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("좌표 정보가 올바르지 않습니다.");
        }
        // 위도 범위: -90 ~ 90
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("위도는 -90에서 90 사이여야 합니다.");
        }
        // 경도 범위: -180 ~ 180
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("경도는 -180에서 180 사이여야 합니다.");
        }
    }
}
