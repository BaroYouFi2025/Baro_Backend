package baro.baro.domain.member.dto;

import lombok.Data;

@Data
public class InvitationRequest {
    private Long inviteeUserId;
    private String relation;
}
