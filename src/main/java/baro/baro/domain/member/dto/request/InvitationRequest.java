package baro.baro.domain.member.dto.request;

import lombok.Data;

@Data
public class InvitationRequest {
    private Long inviteeUserId;
    private String relation;
}
