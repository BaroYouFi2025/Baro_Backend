package baro.baro.domain.missingperson.controller;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
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
}