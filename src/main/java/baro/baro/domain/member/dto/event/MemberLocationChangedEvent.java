package baro.baro.domain.member.dto.event;

import org.springframework.context.ApplicationEvent;

// GPS 위치 변경 시 발행되는 도메인 이벤트
// DeviceServiceImpl에서 위치 업데이트 후 발행됩니다.
public class MemberLocationChangedEvent extends ApplicationEvent {

    private final Long userId;

    public MemberLocationChangedEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}