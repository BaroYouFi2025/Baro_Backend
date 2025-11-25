package baro.baro.domain.device.dto.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LogoutSuccessEvent extends ApplicationEvent {

    Long deviceId;
    public LogoutSuccessEvent(Object source, Long deviceId) {
        super(source);
        this.deviceId = deviceId;
    }
}
