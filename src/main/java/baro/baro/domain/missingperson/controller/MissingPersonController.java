package baro.baro.domain.missingperson.controller;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.ReportSightingRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.dto.res.ReportSightingResponse;
import baro.baro.domain.missingperson.service.MissingPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MissingPerson", description = "실종자 관리 API")
@RestController
@RequestMapping("/missing-persons")
@RequiredArgsConstructor
public class MissingPersonController {

    private final MissingPersonService missingPersonService;

    @Operation(summary = "실종자 등록", 
               description = "새로운 실종자 정보를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "실종자 등록 성공",
            content = @Content(schema = @Schema(implementation = RegisterMissingPersonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterMissingPersonResponse> registerMissingPerson(@Valid @RequestBody RegisterMissingPersonRequest request) {
        RegisterMissingPersonResponse response = missingPersonService.registerMissingPerson(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "실종자 정보 수정", 
               description = "기존 실종자 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "실종자 정보 수정 성공",
            content = @Content(schema = @Schema(implementation = RegisterMissingPersonResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "실종자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PutMapping("/register/{id}")
    public ResponseEntity<RegisterMissingPersonResponse> updateMissingPerson(@PathVariable Long id, @Valid @RequestBody UpdateMissingPersonRequest request) {
        RegisterMissingPersonResponse response = missingPersonService.updateMissingPerson(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "실종자 목록 검색", 
               description = "실종자 목록을 검색합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "실종자 목록 검색 성공",
            content = @Content(schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<MissingPersonResponse>> searchMissingPersons(@Valid SearchMissingPersonRequest request) {
        Page<MissingPersonResponse> response = missingPersonService.searchMissingPersons(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "실종자 상세 조회", 
               description = "실종자 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "실종자 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = MissingPersonDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "실종자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<MissingPersonDetailResponse> getMissingPersonDetail(@PathVariable Long id) {
        MissingPersonDetailResponse response = missingPersonService.getMissingPersonDetail(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "실종자 발견 신고", 
               description = "실종자를 발견했을 때 신고합니다. 신고가 접수되면 실종자 등록자에게 푸시 알림이 전송됩니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "발견 신고 성공",
            content = @Content(schema = @Schema(implementation = ReportSightingResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패 또는 이미 종료된 케이스)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "실종자를 찾을 수 없음 또는 활성 케이스가 없음",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/sightings")
    public ResponseEntity<ReportSightingResponse> reportSighting(
            @Valid @RequestBody ReportSightingRequest request) {
        ReportSightingResponse response = missingPersonService.reportSighting(request);
        return ResponseEntity.ok(response);
    }
}