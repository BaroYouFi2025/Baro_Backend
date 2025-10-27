package baro.baro.domain.common.geocoding.service;

import baro.baro.domain.common.geocoding.dto.GeocodingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Google Maps Geocoding API를 사용한 지오코딩 서비스 구현
 */
@Slf4j
@Service
public class GoogleMapsGeocodingService implements GeocodingService {

    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    @Value("${google.maps.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GoogleMapsGeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getAddressFromCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            log.warn("위도 또는 경도가 null입니다.");
            return "위치 정보 없음";
        }

        try {
            // API URL 구성
            String url = UriComponentsBuilder.fromHttpUrl(GEOCODING_API_URL)
                    .queryParam("latlng", latitude + "," + longitude)
                    .queryParam("key", apiKey)
                    .queryParam("language", "ko") // 한국어 결과
                    .toUriString();

            log.debug("Geocoding API 호출: lat={}, lng={}", latitude, longitude);

            // API 호출
            GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class);

            if (response != null && "OK".equals(response.getStatus()) && 
                response.getResults() != null && !response.getResults().isEmpty()) {
                
                String address = response.getResults().get(0).getFormattedAddress();
                log.info("주소 변환 성공: ({}, {}) -> {}", latitude, longitude, address);
                return address;
            } else {
                log.warn("주소 변환 실패: status={}", response != null ? response.getStatus() : "null");
                return String.format("위치: %.6f, %.6f", latitude, longitude);
            }

        } catch (Exception e) {
            log.error("Geocoding API 호출 중 오류 발생: {}", e.getMessage(), e);
            return String.format("위치: %.6f, %.6f", latitude, longitude);
        }
    }
}
