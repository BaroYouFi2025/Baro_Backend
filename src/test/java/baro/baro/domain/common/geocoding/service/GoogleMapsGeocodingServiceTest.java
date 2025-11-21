package baro.baro.domain.common.geocoding.service;

import baro.baro.domain.common.geocoding.dto.GeocodingResponse;
import baro.baro.domain.common.geocoding.exception.GeocodingErrorCode;
import baro.baro.domain.common.geocoding.exception.GeocodingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleMapsGeocodingServiceTest {

    @Mock
    private RestClient restClient;
    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private GoogleMapsGeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(geocodingService, "apiKey", "fake-key");
        lenient().when(restClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getAddressFromCoordinates_returnsFormattedAddress() {
        GeocodingResponse response = successResponse("서울 중구 세종대로 110", 37.5665, 126.9780);
        when(responseSpec.body(GeocodingResponse.class)).thenReturn(response);

        String address = geocodingService.getAddressFromCoordinates(37.5665, 126.9780);

        assertThat(address).isEqualTo("서울 중구 세종대로 110");
    }

    @Test
    void getAddressFromCoordinates_invalidLatitudeThrowsGeocodingException() {
        assertThatThrownBy(() -> geocodingService.getAddressFromCoordinates(120.0, 10.0))
                .isInstanceOf(GeocodingException.class)
                .extracting("geocodingErrorCode")
                .isEqualTo(GeocodingErrorCode.INVALID_COORDINATES);
    }

    @Test
    void getAddressFromCoordinates_nonOkResponseThrowsGeocodingException() {
        GeocodingResponse response = new GeocodingResponse();
        response.setStatus("ZERO_RESULTS");
        response.setResults(Collections.emptyList());
        when(responseSpec.body(GeocodingResponse.class)).thenReturn(response);

        assertThatThrownBy(() -> geocodingService.getAddressFromCoordinates(37.0, 127.0))
                .isInstanceOf(GeocodingException.class)
                .extracting("geocodingErrorCode")
                .isEqualTo(GeocodingErrorCode.ADDRESS_NOT_FOUND);
    }

    @Test
    void getAddressFromCoordinates_networkFailureThrowsServiceUnavailable() {
        when(responseSpec.body(GeocodingResponse.class)).thenThrow(new RestClientException("timeout"));

        assertThatThrownBy(() -> geocodingService.getAddressFromCoordinates(37.0, 127.0))
                .isInstanceOf(GeocodingException.class)
                .extracting("geocodingErrorCode")
                .isEqualTo(GeocodingErrorCode.GEOCODING_SERVICE_UNAVAILABLE);
    }

    @Test
    void getPointFromAddress_returnsPoint() {
        GeocodingResponse response = successResponse("서울역", 37.5563, 126.9723);
        when(responseSpec.body(GeocodingResponse.class)).thenReturn(response);

        Point point = geocodingService.getPointFromAddress("서울역");

        assertThat(point.getY()).isEqualTo(37.5563);
        assertThat(point.getX()).isEqualTo(126.9723);
    }

    @Test
    void getPointFromAddress_blankAddressThrowsGeocodingException() {
        assertThatThrownBy(() -> geocodingService.getPointFromAddress("   "))
                .isInstanceOf(GeocodingException.class)
                .extracting("geocodingErrorCode")
                .isEqualTo(GeocodingErrorCode.INVALID_ADDRESS);
    }

    @Test
    void getPointFromAddress_nonOkResponseThrowsGeocodingException() {
        GeocodingResponse response = new GeocodingResponse();
        response.setStatus("ZERO_RESULTS");
        response.setResults(Collections.emptyList());
        when(responseSpec.body(GeocodingResponse.class)).thenReturn(response);

        assertThatThrownBy(() -> geocodingService.getPointFromAddress("없는 주소"))
                .isInstanceOf(GeocodingException.class)
                .extracting("geocodingErrorCode")
                .isEqualTo(GeocodingErrorCode.COORDINATES_NOT_FOUND);
    }

    private GeocodingResponse successResponse(String formattedAddress, double lat, double lng) {
        GeocodingResponse response = new GeocodingResponse();
        GeocodingResponse.Location location = new GeocodingResponse.Location();
        location.setLat(lat);
        location.setLng(lng);

        GeocodingResponse.Geometry geometry = new GeocodingResponse.Geometry();
        geometry.setLocation(location);

        GeocodingResponse.Result result = new GeocodingResponse.Result();
        result.setFormattedAddress(formattedAddress);
        result.setGeometry(geometry);

        response.setStatus("OK");
        response.setResults(List.of(result));
        return response;
    }
}
