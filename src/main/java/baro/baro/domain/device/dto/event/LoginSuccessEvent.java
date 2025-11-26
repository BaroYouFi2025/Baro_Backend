package baro.baro.domain.device.dto.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LoginSuccessEvent extends ApplicationEvent {

    private final String uid;
    private final String deviceUuid;

    public LoginSuccessEvent(Object source, String uid, String deviceUuid) {
        super(source);
        this.uid = uid;
        this.deviceUuid = deviceUuid;
    }
}
