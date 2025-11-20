package baro.baro.domain.member.service;

import baro.baro.domain.member.dto.event.MemberLocationEvent;
import baro.baro.domain.member.dto.res.MemberLocationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MemberLocationEmitterRegistry 테스트")
class MemberLocationEmitterRegistryTest {

    private MemberLocationEmitterRegistry registry;
    private MemberLocationEvent sampleEvent;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        registry = new MemberLocationEmitterRegistry(mapper);
        MemberLocationResponse.LocationInfo location = MemberLocationResponse.LocationInfo.create(37.0, 127.0);
        MemberLocationResponse response = MemberLocationResponse.create(10L, "구성원", "가족", 90, 0.0, location);
        sampleEvent = MemberLocationEvent.update(List.of(response));
    }

    @Test
    @DisplayName("Emitter 등록 및 제거 시 연결 수가 업데이트된다")
    void addEmitterAndRemoveEmitter() {
        RecordingEmitter emitter = new RecordingEmitter();
        registry.addEmitter(1L, emitter);

        assertThat(registry.hasConnection(1L)).isTrue();
        assertThat(registry.getTotalConnectionCount()).isEqualTo(1);

        registry.removeEmitter(1L, emitter);

        assertThat(registry.hasConnection(1L)).isFalse();
        assertThat(registry.getTotalConnectionCount()).isZero();
    }

    @Test
    @DisplayName("Emitter가 등록되어 있으면 broadcast로 이벤트를 전달한다")
    void broadcastSendsEvent() {
        RecordingEmitter emitter = new RecordingEmitter();
        registry.addEmitter(1L, emitter);

        registry.broadcast(1L, sampleEvent);

        assertThat(emitter.getSendCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("전송 중 예외가 발생한 Emitter는 제거된다")
    void broadcastRemovesFailingEmitter() {
        FailingEmitter emitter = new FailingEmitter();
        registry.addEmitter(1L, emitter);

        registry.broadcast(1L, sampleEvent);

        assertThat(registry.hasConnection(1L)).isFalse();
    }

    @Test
    @DisplayName("여러 사용자에게 동시에 브로드캐스트한다")
    void broadcastToUsersSendsToEachUser() {
        RecordingEmitter emitter1 = new RecordingEmitter();
        RecordingEmitter emitter2 = new RecordingEmitter();
        registry.addEmitter(1L, emitter1);
        registry.addEmitter(2L, emitter2);

        registry.broadcastToUsers(List.of(1L, 2L), sampleEvent);

        assertThat(emitter1.getSendCount()).isEqualTo(1);
        assertThat(emitter2.getSendCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Heartbeat 전송은 특정 사용자 및 전체 사용자에게 적용된다")
    void sendHeartbeatMethods() {
        RecordingEmitter emitter1 = new RecordingEmitter();
        RecordingEmitter emitter2 = new RecordingEmitter();
        registry.addEmitter(1L, emitter1);
        registry.addEmitter(2L, emitter2);

        registry.sendHeartbeat(1L);
        assertThat(emitter1.getSendCount()).isEqualTo(1);
        assertThat(emitter2.getSendCount()).isZero();

        registry.sendHeartbeatToAll();
        assertThat(emitter1.getSendCount()).isEqualTo(2);
        assertThat(emitter2.getSendCount()).isEqualTo(1);
    }

    private static class RecordingEmitter extends SseEmitter {
        private final List<SseEventBuilder> sentEvents = new CopyOnWriteArrayList<>();

        RecordingEmitter() {
            super(Long.MAX_VALUE);
        }

        @Override
        public void send(SseEventBuilder builder) {
            sentEvents.add(builder);
        }

        int getSendCount() {
            return sentEvents.size();
        }
    }

    private static class FailingEmitter extends SseEmitter {
        FailingEmitter() {
            super(Long.MAX_VALUE);
        }

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            throw new IOException("전송 실패");
        }
    }
}
