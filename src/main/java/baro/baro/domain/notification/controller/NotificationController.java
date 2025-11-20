package baro.baro.domain.notification.controller;

import baro.baro.domain.notification.dto.res.NotificationResponse;
import baro.baro.domain.notification.service.NotificationServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 알림 관리 REST API 컨트롤러
// 사용자의 알림을 조회하고 관리하는 기능을 제공합니다.
// 모든 엔드포인트는 JWT 인증이 필요합니다.
@Tag(name = "Notification", description = "알림 관리 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class NotificationController {

    private final NotificationServiceInterface notificationService;

    @Operation(summary = "내 알림 목록 조회", description = "현재 로그인한 사용자의 모든 알림을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "알림 조회 성공",
            content = @Content(schema = @Schema(implementation = NotificationResponse[].class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        List<NotificationResponse> notifications = notificationService.getMyNotifications();
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "읽지 않은 알림 목록 조회", description = "현재 로그인한 사용자의 읽지 않은 알림만 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "읽지 않은 알림 조회 성공",
            content = @Content(schema = @Schema(implementation = NotificationResponse[].class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications();
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "읽지 않은 알림 개수 조회", description = "현재 로그인한 사용자의 읽지 않은 알림 개수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "개수 조회 성공",
            content = @Content(schema = @Schema(implementation = Long.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        long unreadCount = notificationService.getUnreadCount();
        return ResponseEntity.ok(unreadCount);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "읽음 처리 성공"
        ),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 알림 또는 권한 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}
