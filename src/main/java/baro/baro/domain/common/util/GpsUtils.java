package baro.baro.domain.common.util;

import org.locationtech.jts.geom.Point;

// GPS 관련 유틸리티 클래스
//
// 거리 계산, 좌표 변환 등의 GPS 관련 유틸리티 메서드를 제공합니다.
public class GpsUtils {

    private static final double EARTH_RADIUS_KM = 6371.0; // 지구 반지름 (km)

    // 두 GPS 좌표 간의 거리를 계산합니다 (Haversine 공식 사용).
    //
    // @param lat1 첫 번째 위치의 위도
    // @param lon1 첫 번째 위치의 경도
    // @param lat2 두 번째 위치의 위도
    // @param lon2 두 번째 위치의 경도
    // @return 두 지점 간의 거리 (km)
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 위도와 경도를 라디안으로 변환
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 위도와 경도의 차이
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // Haversine 공식
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 거리 계산 (km)
        double distance = EARTH_RADIUS_KM * c;

        // 소수점 둘째 자리까지 반올림
        return Math.round(distance * 100.0) / 100.0;
    }

    // PostGIS Point 객체 간의 거리를 계산합니다.
    //
    // @param point1 첫 번째 위치
    // @param point2 두 번째 위치
    // @return 두 지점 간의 거리 (km)
    public static double calculateDistance(Point point1, Point point2) {
        if (point1 == null || point2 == null) {
            return 0.0;
        }

        // Point는 (경도, 위도) 순서로 저장됨
        double lon1 = point1.getX();
        double lat1 = point1.getY();
        double lon2 = point2.getX();
        double lat2 = point2.getY();

        return calculateDistance(lat1, lon1, lat2, lon2);
    }

    // PostGIS Point 객체에서 위도를 추출합니다.
    //
    // @param point PostGIS Point
    // @return 위도
    public static Double getLatitude(Point point) {
        return point != null ? point.getY() : null;
    }

    // PostGIS Point 객체에서 경도를 추출합니다.
    //
    // @param point PostGIS Point
    // @return 경도
    public static Double getLongitude(Point point) {
        return point != null ? point.getX() : null;
    }
}
