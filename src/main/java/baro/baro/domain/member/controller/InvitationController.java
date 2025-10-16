package baro.baro.domain.member.controller;

import baro.baro.domain.member.dto.request.AcceptInvitationRequest;
import baro.baro.domain.member.dto.request.InvitationRequest;
import baro.baro.domain.member.dto.request.RejectInvitationRequest;
import baro.baro.domain.member.dto.response.AcceptInvitationResponse;
import baro.baro.domain.member.dto.response.InvitationResponse;
import baro.baro.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Invitation", description = "멤버 초대 관리 API")
@RestController
@RequestMapping("/members/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final MemberService memberService;

    @Operation(summary = "멤버 초대", description = "다른 사용자를 멤버로 초대합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "초대 성공",
            content = @Content(schema = @Schema(implementation = InvitationResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "초대할 사용자를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<InvitationResponse> invitations(@RequestBody InvitationRequest request) {
        InvitationResponse response = memberService.makeInvitation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "초대 수락", description = "받은 초대를 수락하여 관계를 맺습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "초대 수락 성공",
            content = @Content(schema = @Schema(implementation = AcceptInvitationResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (상태가 PENDING이 아님)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "초대 대상자가 아님",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "초대를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @PostMapping("/acceptance")
    public ResponseEntity<AcceptInvitationResponse> acceptInvitation(@RequestBody AcceptInvitationRequest request) {
         AcceptInvitationResponse response = memberService.acceptInvitation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "초대 거절", description = "받은 초대를 거절합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "초대 거절 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (상태가 PENDING이 아님)",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "초대 대상자가 아님",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "초대를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(implementation = baro.baro.domain.common.exception.ApiErrorResponse.class)))
    })
    @DeleteMapping("/rejection")
    public  ResponseEntity<?> rejectInvitation(@RequestBody RejectInvitationRequest request) {
        memberService.rejectInvitation(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}