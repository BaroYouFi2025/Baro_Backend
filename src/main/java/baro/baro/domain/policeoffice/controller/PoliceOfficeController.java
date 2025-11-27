package baro.baro.domain.policeoffice.controller;

import baro.baro.domain.policeoffice.dto.req.NearbyPoliceOfficeRequest;
import baro.baro.domain.policeoffice.dto.res.PoliceOfficeResponse;
import baro.baro.domain.policeoffice.service.PoliceOfficeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/police-offices")
@Tag(name = "PoliceOffice", description = "경찰관서 검색 API")
public class PoliceOfficeController {

    private final PoliceOfficeService policeOfficeService;

    @GetMapping("/nearby")
    @Operation(summary = "근처 경찰관서 조회", description = "현재 GPS 좌표를 기준으로 가까운 지구대/파출소 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PoliceOfficeResponse.class)))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    public ResponseEntity<List<PoliceOfficeResponse>> findNearby(
            @Valid @ParameterObject NearbyPoliceOfficeRequest request) {
        List<PoliceOfficeResponse> responses = policeOfficeService.findNearbyOffices(request);

        return ResponseEntity.ok(responses);
    }
}
