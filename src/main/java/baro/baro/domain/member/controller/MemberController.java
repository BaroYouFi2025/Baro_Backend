package baro.baro.domain.member.controller;

import baro.baro.domain.auth.service.JwtTokenProvider;
import baro.baro.domain.member.dto.event.MemberLocationEvent;
import baro.baro.domain.member.dto.res.MemberLocationResponse;
import baro.baro.domain.member.service.MemberLocationEmitterRegistry;
import baro.baro.domain.member.service.MemberService;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



@Slf4j
@Tag(name = "Member", description = "멤버 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분
    private static final long HEARTBEAT_INTERVAL = 15 * 1000L; // 15초

    private final MemberService memberService;
    private final MemberLocationEmitterRegistry emitterRegistry;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

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

    @Operation(summary = "구성원 위치 실시간 스트림", description = "SSE를 통해 구성원 위치 변경을 실시간으로 수신합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "스트림 연결 성공",
            content = @Content(schema = @Schema(implementation = MemberLocationEvent.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping(value = "/locations/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMemberLocations(
            @RequestParam("token") String token
    ) {
        // 토큰 검증 및 사용자 조회
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        
        String uid = jwtTokenProvider.getSubjectFromToken(token);
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Long userId = user.getId();

        // SseEmitter 생성 (타임아웃 30분)
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 레지스트리에 등록
        emitterRegistry.addEmitter(userId, emitter);

        // 콜백 설정
        emitter.onCompletion(() -> {
            log.debug("SSE 연결 완료 - userId: {}", userId);
            emitterRegistry.removeEmitter(userId, emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE 타임아웃 - userId: {}", userId);
            emitterRegistry.removeEmitter(userId, emitter);
            emitter.complete();
        });

        emitter.onError(e -> {
            log.warn("SSE 오류 - userId: {}, error: {}", userId, e.getMessage());
            emitterRegistry.removeEmitter(userId, emitter);
        });

        // 초기 데이터 전송
        try {
            List<MemberLocationResponse> initialData = memberService.getMemberLocationsForUser(userId);
            MemberLocationEvent initialEvent = MemberLocationEvent.initial(initialData);
            String eventData = objectMapper.writeValueAsString(initialEvent);

            emitter.send(SseEmitter.event()
                    .name("location")
                    .data(eventData));

            log.debug("초기 데이터 전송 완료 - userId: {}, 구성원 수: {}", userId, initialData.size());
        } catch (IOException e) {
            log.error("초기 데이터 전송 실패 - userId: {}", userId, e);
            emitter.completeWithError(e);
            return emitter;
        }

        // Heartbeat 스케줄러 설정
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                MemberLocationEvent heartbeat = MemberLocationEvent.heartbeat();
                String heartbeatData = objectMapper.writeValueAsString(heartbeat);
                emitter.send(SseEmitter.event()
                        .name("location")
                        .data(heartbeatData));
            } catch (IOException e) {
                log.debug("Heartbeat 전송 실패, 연결 종료 - userId: {}", userId);
                scheduler.shutdown();
                emitterRegistry.removeEmitter(userId, emitter);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);

        // emitter 완료 시 스케줄러 종료
        emitter.onCompletion(scheduler::shutdown);
        emitter.onTimeout(scheduler::shutdown);
        emitter.onError(e -> scheduler.shutdown());

        return emitter;
    }

}
