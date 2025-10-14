package baro.baro.domain.member.dto.request;

import lombok.Data;

@Data
public class AcceptInvitationRequest {
    private Long relationshipRequestId;
    private String relation;
}
