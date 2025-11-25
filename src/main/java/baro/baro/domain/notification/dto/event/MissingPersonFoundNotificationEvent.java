package baro.baro.domain.notification.dto.event;

import baro.baro.domain.user.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

// 실종자 발견 신고 알림 이벤트
@Getter
public class MissingPersonFoundNotificationEvent extends ApplicationEvent {

    private final Long sightingId;
    private final User missingPersonOwner;
    private final String missingPersonName;
    private final String reporterName;
    private final String address;

    public MissingPersonFoundNotificationEvent(Object source,
                                               Long sightingId,
                                               User missingPersonOwner,
                                               String missingPersonName,
                                               String reporterName,
                                               String address) {
        super(source);
        this.sightingId = sightingId;
        this.missingPersonOwner = missingPersonOwner;
        this.missingPersonName = missingPersonName;
        this.reporterName = reporterName;
        this.address = address;
    }
}
