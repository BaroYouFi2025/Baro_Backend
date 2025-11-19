package baro.baro.domain.member.dto.event;

import baro.baro.domain.member.dto.res.MemberLocationResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// SSE 이벤트 DTO
// 클라이언트에게 전송되는 위치 이벤트 데이터를 포함합니다.
@Data
@Schema(description = "구성원 위치 SSE 이벤트")
public class MemberLocationEvent {

    @Schema(description = "이벤트 타입", example = "UPDATE", allowableValues = {"INITIAL", "UPDATE", "HEARTBEAT"})
    private EventType type;

    @Schema(description = "이벤트 발생 시간")
    private LocalDateTime timestamp;

    @Schema(description = "위치 데이터 (HEARTBEAT 타입에서는 null)")
    private List<MemberLocationResponse> payload;

    public enum EventType {
        INITIAL,    // 초기 연결 시 전체 데이터
        UPDATE,     // 위치 업데이트
        HEARTBEAT   // 연결 유지용
    }

    public static MemberLocationEvent initial(List<MemberLocationResponse> payload) {
        MemberLocationEvent event = new MemberLocationEvent();
        event.type = EventType.INITIAL;
        event.timestamp = LocalDateTime.now();
        event.payload = payload;
        return event;
    }

    public static MemberLocationEvent update(List<MemberLocationResponse> payload) {
        MemberLocationEvent event = new MemberLocationEvent();
        event.type = EventType.UPDATE;
        event.timestamp = LocalDateTime.now();
        event.payload = payload;
        return event;
    }

    public static MemberLocationEvent heartbeat() {
        MemberLocationEvent event = new MemberLocationEvent();
        event.type = EventType.HEARTBEAT;
        event.timestamp = LocalDateTime.now();
        event.payload = null;
        return event;
    }
}