package baro.baro.domain.member.dto;

import lombok.Data;

@Data
public class AcceptInvitationRequest {
    private Long relationshipRequestId;
    private String relation;
}
