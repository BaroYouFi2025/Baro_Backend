package baro.baro.domain.common.geocoding.controller;

import baro.baro.domain.common.geocoding.dto.AddressResponse;
import baro.baro.domain.common.geocoding.service.GeocodingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 위치 정보 관련 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
@Tag(name = "Location", description = "위치 정보 API")
public class LocationController {

    private final GeocodingService geocodingService;

    /**
     * 위도/경도를 주소로 변환 (Reverse Geocoding)
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @return 변환된 주소 정보
     */
    @GetMapping("/address")
    @Operation(
        summary = "좌표를 주소로 변환",
        description = "위도와 경도를 입력받아 해당 위치의 주소를 반환합니다. Google Maps Geocoding API를 사용합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "주소 변환 성공",
            content = @Content(schema = @Schema(implementation = AddressResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (유효하지 않은 좌표)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류 또는 Geocoding API 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    public ResponseEntity<AddressResponse> getAddressFromCoordinates(
            @Parameter(description = "위도", example = "37.5665", required = true)
            @RequestParam Double latitude,

            @Parameter(description = "경도", example = "126.9780", required = true)
            @RequestParam Double longitude) {

        log.info("주소 변환 요청: latitude={}, longitude={}", latitude, longitude);

        String address = geocodingService.getAddressFromCoordinates(latitude, longitude);
        AddressResponse response = AddressResponse.create(latitude, longitude, address, true);

        log.info("주소 변환 성공: {}", address);
        return ResponseEntity.ok(response);
    }
}
