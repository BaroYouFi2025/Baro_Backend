package baro.baro.domain.missingperson.service;

import baro.baro.domain.common.exception.BusinessException;
import baro.baro.domain.common.geocoding.service.GeocodingService;
import baro.baro.domain.common.util.LocationUtil;
import baro.baro.domain.missingperson.exception.MissingPersonErrorCode;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

// 위치 관련 도메인 서비스
// 위치 정보 변환 및 검증 로직을 캡슐화
@Slf4j
@Component
@RequiredArgsConstructor
public class LocationService {

    private final GeocodingService geocodingService;

    // 좌표를 주소로 변환
    public String getAddressFromCoordinates(Double latitude, Double longitude) {
        return geocodingService.getAddressFromCoordinates(latitude, longitude);
    }

    // 좌표에서 Point 객체 생성 (검증 포함)
    // @param latitude 위도
    // @param longitude 경도
    // @return Point 객체
    public Point createPoint(Double latitude, Double longitude) {
        return LocationUtil.createPoint(latitude, longitude);
    }

    // 좌표 -> 주소 + Point 객체 생성
    public LocationInfo createLocationInfo(Double latitude, Double longitude) {
        String address = getAddressFromCoordinates(latitude, longitude);
        Point point = createPoint(latitude, longitude);
        return new LocationInfo(address, point);
    }

    // 위치 정보 (주소 + Point)
    public record LocationInfo(String address, Point point) {
    }
}
