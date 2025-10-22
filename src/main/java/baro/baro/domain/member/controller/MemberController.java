package baro.baro.domain.member.controller;

import baro.baro.domain.member.dto.response.MemberLocationResponse;
import baro.baro.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Member", description = "멤버 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "구성원 위치 조회", description = "사용자와 관계가 있는 구성원들의 위치, 배터리, 거리 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberLocationResponse.class)))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/locations")
    public ResponseEntity<List<MemberLocationResponse>> getMemberLocations() {
        List<MemberLocationResponse> response = memberService.getMemberLocations();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
