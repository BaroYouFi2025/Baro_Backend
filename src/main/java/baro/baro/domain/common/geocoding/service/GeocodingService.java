package baro.baro.domain.common.geocoding.service;

/**
 * 지오코딩 서비스 인터페이스
 * 위도/경도 좌표를 주소로 변환하거나 주소를 좌표로 변환하는 기능 제공
 */
public interface GeocodingService {
    
    /**
     * 위도/경도를 주소로 변환 (Reverse Geocoding)
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @return 주소 문자열
     */
    String getAddressFromCoordinates(Double latitude, Double longitude);
}
