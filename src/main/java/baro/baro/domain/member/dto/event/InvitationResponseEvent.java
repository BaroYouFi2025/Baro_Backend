package baro.baro.domain.member.dto.event;

import baro.baro.domain.user.entity.User;
import org.springframework.context.ApplicationEvent;

// 초대 응답 이벤트 (수락/거절 알림 발송용)
public class InvitationResponseEvent extends ApplicationEvent {

    private final User inviter;
    private final User invitee;
    private final boolean accepted;
    private final String relation;

    public InvitationResponseEvent(Object source, User inviter, User invitee, boolean accepted, String relation) {
        super(source);
        this.inviter = inviter;
        this.invitee = invitee;
        this.accepted = accepted;
        this.relation = relation;
    }

    public User getInviter() {
        return inviter;
    }

    public User getInvitee() {
        return invitee;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getRelation() {
        return relation;
    }
}
