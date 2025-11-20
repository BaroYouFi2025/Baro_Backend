package baro.baro.domain.notification.controller;

import baro.baro.domain.member.dto.res.AcceptInvitationResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.dto.res.SightingDetailResponse;
import baro.baro.domain.notification.dto.req.AcceptInvitationFromNotificationRequest;
import baro.baro.domain.notification.service.NotificationServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// 알림 액션 REST API 컨트롤러
// 알림을 통해 초대 수락/거절, 실종자 조회 등의 액션을 수행합니다.
// 모든 엔드포인트는 JWT 인증이 필요합니다.
@Tag(name = "Notification Action", description = "알림 액션 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class NotificationActionController {

    private final NotificationServiceInterface notificationService;

    @Operation(summary = "알림을 통한 초대 수락", description = "INVITE_REQUEST 타입 알림에서 직접 초대를 수락합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "초대 수락 성공",
            content = @Content(schema = @Schema(implementation = AcceptInvitationResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 알림 타입 또는 유효하지 않은 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @PostMapping("/{notificationId}/accept-invitation")
    public ResponseEntity<AcceptInvitationResponse> acceptInvitationFromNotification(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long notificationId,
            @Valid @RequestBody AcceptInvitationFromNotificationRequest request) {
        AcceptInvitationResponse response = notificationService.acceptInvitationFromNotification(notificationId, request.getRelation());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "알림을 통한 초대 거절", description = "INVITE_REQUEST 타입 알림에서 직접 초대를 거절합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "초대 거절 성공"
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 알림 타입"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @PostMapping("/{notificationId}/reject-invitation")
    public ResponseEntity<Void> rejectInvitationFromNotification(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long notificationId) {
        notificationService.rejectInvitationFromNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림을 통한 실종자 상세 조회", description = "FOUND_REPORT 또는 NEARBY_ALERT 타입 알림에서 관련 실종자 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "실종자 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = MissingPersonDetailResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 알림 타입"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "알림 또는 실종자를 찾을 수 없음")
    })
    @GetMapping("/{notificationId}/missing-person")
    public ResponseEntity<MissingPersonDetailResponse> getMissingPersonDetailFromNotification(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long notificationId) {
        MissingPersonDetailResponse response = notificationService.getMissingPersonDetailFromNotification(notificationId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "알림을 통한 발견 신고 상세 조회", description = "FOUND_REPORT 타입 알림에서 관련 발견 신고 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "발견 신고 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = SightingDetailResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 알림 타입"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "알림 또는 발견 신고를 찾을 수 없음")
    })
    @GetMapping("/{notificationId}/sighting")
    public ResponseEntity<SightingDetailResponse> getSightingDetailFromNotification(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long notificationId) {
        SightingDetailResponse response = notificationService.getSightingDetailFromNotification(notificationId);
        return ResponseEntity.ok(response);
    }
}
