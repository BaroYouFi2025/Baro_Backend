package baro.baro.domain.notification.dto.event;

import baro.baro.domain.user.entity.User;
import lombok.Getter;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEvent;

// 주변 실종자 알림 이벤트
@Getter
public class NearbyAlertNotificationEvent extends ApplicationEvent {

    private final User reporter;
    private final String missingPersonName;
    private final double distance;
    private final Point reporterLocation;
    private final Long missingPersonId;

    public NearbyAlertNotificationEvent(Object source,
                                        User reporter,
                                        String missingPersonName,
                                        double distance,
                                        Point reporterLocation,
                                        Long missingPersonId) {
        super(source);
        this.reporter = reporter;
        this.missingPersonName = missingPersonName;
        this.distance = distance;
        this.reporterLocation = reporterLocation;
        this.missingPersonId = missingPersonId;
    }
}
