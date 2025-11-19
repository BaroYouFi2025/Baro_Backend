package baro.baro.domain.member.service;

import baro.baro.domain.member.dto.event.MemberLocationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// SSE Emitter 레지스트리
// 사용자별 SSE 연결을 관리하고 브로드캐스트를 처리합니다.
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLocationEmitterRegistry {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    // 새로운 SSE Emitter를 등록합니다.
    //
    // @param userId 사용자 ID
    // @param emitter SSE Emitter
    public void addEmitter(Long userId, SseEmitter emitter) {
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.debug("SSE Emitter 등록 - userId: {}, 총 연결 수: {}", userId, emitters.get(userId).size());
    }

    // SSE Emitter를 제거합니다.
    //
    // @param userId 사용자 ID
    // @param emitter 제거할 SSE Emitter
    public void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
            log.debug("SSE Emitter 제거 - userId: {}, 남은 연결 수: {}", userId,
                    userEmitters.isEmpty() ? 0 : userEmitters.size());
        }
    }

    // 특정 사용자에게 이벤트를 브로드캐스트합니다.
    //
    // @param userId 사용자 ID
    // @param event 전송할 이벤트
    public void broadcast(Long userId, MemberLocationEvent event) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        // 이벤트를 한 번만 직렬화
        String eventData;
        try {
            eventData = objectMapper.writeValueAsString(event);
        } catch (IOException e) {
            log.error("이벤트 직렬화 실패 - userId: {}", userId, e);
            return;
        }

        // 실패한 emitter 목록
        List<SseEmitter> failedEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("location")
                        .data(eventData));
            } catch (IOException e) {
                log.warn("SSE 전송 실패 - userId: {}, 이유: {}", userId, e.getMessage());
                failedEmitters.add(emitter);
            }
        }

        // 실패한 emitter 제거
        for (SseEmitter failed : failedEmitters) {
            removeEmitter(userId, failed);
        }
    }

    // 여러 사용자에게 이벤트를 브로드캐스트합니다.
    //
    // @param userIds 사용자 ID 목록
    // @param event 전송할 이벤트
    public void broadcastToUsers(List<Long> userIds, MemberLocationEvent event) {
        for (Long userId : userIds) {
            broadcast(userId, event);
        }
    }

    // 특정 사용자의 모든 연결에 heartbeat를 전송합니다.
    //
    // @param userId 사용자 ID
    public void sendHeartbeat(Long userId) {
        broadcast(userId, MemberLocationEvent.heartbeat());
    }

    // 모든 연결에 heartbeat를 전송합니다.
    public void sendHeartbeatToAll() {
        MemberLocationEvent heartbeat = MemberLocationEvent.heartbeat();
        for (Long userId : emitters.keySet()) {
            broadcast(userId, heartbeat);
        }
    }

    // 특정 사용자가 연결되어 있는지 확인합니다.
    //
    // @param userId 사용자 ID
    // @return 연결 여부
    public boolean hasConnection(Long userId) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        return userEmitters != null && !userEmitters.isEmpty();
    }

    // 현재 활성 연결 수를 반환합니다.
    //
    // @return 총 활성 연결 수
    public int getTotalConnectionCount() {
        return emitters.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}
