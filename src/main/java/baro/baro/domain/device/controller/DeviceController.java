package baro.baro.domain.device.controller;

import baro.baro.domain.device.dto.request.DeviceRegisterRequest;
import baro.baro.domain.device.dto.request.FcmTokenUpdateRequest;
import baro.baro.domain.device.dto.request.GpsUpdateRequest;
import baro.baro.domain.device.dto.response.DeviceResponse;
import baro.baro.domain.device.dto.response.GpsUpdateResponse;
import baro.baro.domain.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 기기 관리 및 GPS 추적 REST API 컨트롤러
 *
 * 모바일 기기 등록 및 GPS 위치 업데이트 기능을 제공합니다.
 * 모든 엔드포인트는 JWT 인증이 필요합니다.
 */
@Tag(name = "Device", description = "기기 관리 API")
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "기기 등록", description = "새로운 기기를 등록합니다. 클라이언트가 생성한 UUID를 사용합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "기기 등록 성공",
            content = @Content(schema = @Schema(implementation = DeviceResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 등록된 UUID)"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/register")
    public ResponseEntity<DeviceResponse> registerDevice(
            @Valid @RequestBody DeviceRegisterRequest request,
            Authentication authentication) {
        String uid = authentication.getName();
        DeviceResponse response = deviceService.registerDevice(uid, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "GPS 위치 업데이트", description = "기기의 GPS 위치를 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "GPS 업데이트 성공",
            content = @Content(schema = @Schema(implementation = GpsUpdateResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "기기를 찾을 수 없음")
    })
    @PostMapping("/{deviceId}/gps")
    public ResponseEntity<GpsUpdateResponse> updateGps(
            @PathVariable Long deviceId,
            @Valid @RequestBody GpsUpdateRequest request,
            Authentication authentication) {
        String uid = authentication.getName();
        GpsUpdateResponse response = deviceService.updateGps(uid, deviceId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "FCM 토큰 업데이트", description = "기기의 FCM 토큰을 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "FCM 토큰 업데이트 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "기기를 찾을 수 없음")
    })
    @PostMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @Valid @RequestBody FcmTokenUpdateRequest request,
            Authentication authentication) {
        String uid = authentication.getName();
        deviceService.updateFcmToken(uid, request);
        return ResponseEntity.ok().build();
    }

}
