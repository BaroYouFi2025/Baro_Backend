package baro.baro.domain.common.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class LocationUtil {
    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    // 위도(latitude), 경도(longitude)로 Point 객체 생성
    public static Point createPoint(double latitude, double longitude) {
        // 주의: Coordinate(longitude, latitude) 순서!
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    // Point 객체에서 위도(latitude)
    public static double getLatitude(Point point) {
        return point.getY();
    }

    // Point 객체에서 경도(longitude)
    public static double getLongitude(Point point) {
        return point.getX();
    }
}
