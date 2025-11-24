package baro.baro.domain.member.dto.event;

import baro.baro.domain.user.entity.User;
import org.springframework.context.ApplicationEvent;

// 초대 생성 이벤트 (도메인 트랜잭션 종료 후 푸시 발송용)
public class InvitationCreatedEvent extends ApplicationEvent {

    private final User invitee;
    private final User inviter;
    private final String relation;
    private final Long invitationId;

    public InvitationCreatedEvent(Object source, User invitee, User inviter, String relation, Long invitationId) {
        super(source);
        this.invitee = invitee;
        this.inviter = inviter;
        this.relation = relation;
        this.invitationId = invitationId;
    }

    public User getInvitee() {
        return invitee;
    }

    public User getInviter() {
        return inviter;
    }

    public String getRelation() {
        return relation;
    }

    public Long getInvitationId() {
        return invitationId;
    }
}
