package baro.baro.domain.missingperson.controller;

import baro.baro.domain.missingperson.dto.req.NearbyMissingPersonRequest;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.service.MissingPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "NearbyMissingPerson", description = "근처 실종자 조회 API")
@RestController
@RequestMapping("/missing-person")
@RequiredArgsConstructor
public class NearbyMissingPersonController {

    private final MissingPersonService missingPersonService;

    @Operation(
        summary = "근처 실종자 조회", 
        description = "현재 위치 기준으로 반경 내의 실종자를 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "근처 실종자 조회 성공",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @GetMapping("/nearby")
    public ResponseEntity<Page<MissingPersonResponse>> findNearbyMissingPersons(
            @Parameter(description = "위도", example = "35.1763", required = true)
            @RequestParam Double latitude,
            @Parameter(description = "경도", example = "128.9664", required = true)
            @RequestParam Double longitude,
            @Parameter(description = "반경 (미터)", example = "1000", required = true)
            @RequestParam Integer radius) {
        NearbyMissingPersonRequest request = NearbyMissingPersonRequest.create(latitude, longitude, radius);
        Page<MissingPersonResponse> response = missingPersonService.findNearbyMissingPersons(request);
        return ResponseEntity.ok(response);
    }
}
