package baro.baro.domain.missingperson.controller;

import baro.baro.domain.missingperson.dto.res.MissingPersonPoliceDetailResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonPoliceResponse;
import baro.baro.domain.missingperson.entity.MissingPersonPolice;
import baro.baro.domain.missingperson.service.PoliceApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Police API", description = "경찰청 실종자 데이터 동기화 API")
@Slf4j
@RestController
@RequestMapping("/missing/police")
@RequiredArgsConstructor
@Validated
public class PoliceApiController {

    private final PoliceApiService policeApiService;

    @Operation(summary = "경찰청 실종자 데이터 수동 동기화 (테스트용)",
            description = "경찰청 API로부터 실종자 데이터를 즉시 동기화합니다. (관리자 권한 필요)")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "동기화 성공",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 전용)",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "동기화 실패 (API 오류 또는 서버 오류)",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncMissingPersonsNow() {
        log.info("경찰청 실종자 데이터 수동 동기화 요청");

        policeApiService.syncMissingPersonsFromPoliceApi();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "경찰청 실종자 데이터 동기화 완료");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "동기화된 경찰청 실종자 목록 조회 (페이징)",
            description = "DB에 저장된 경찰청 실종자 데이터를 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @GetMapping("/missing-persons")
    public ResponseEntity<Page<MissingPersonPoliceResponse>> getAllMissingPersons(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.") Integer page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.") @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.") Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MissingPersonPoliceResponse> response = policeApiService.getAllMissingPersons(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "경찰청 실종자 개별 조회",
            description = "경찰청 실종자 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MissingPersonPoliceDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "실종자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @GetMapping("/missing-persons/{id}")
    public ResponseEntity<MissingPersonPoliceDetailResponse> getMissingPersonById(
            @Parameter(description = "경찰청 실종자 ID", example = "123456789", required = true)
            @PathVariable Long id
    ) {
        MissingPersonPolice missingPerson = policeApiService.getMissingPersonById(id);
        MissingPersonPoliceDetailResponse response = MissingPersonPoliceDetailResponse.from(missingPerson);
        return ResponseEntity.ok(response);
    }

}
