package baro.baro.domain.member.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AcceptInvitationResponse {
    private List<Long> relationshipIds;

    static public AcceptInvitationResponse of(Long oRelationshipId, Long rRelationshipId) {
        AcceptInvitationResponse response = new AcceptInvitationResponse();
        response.relationshipIds = new ArrayList<>();
        response.relationshipIds.add(oRelationshipId);
        response.relationshipIds.add(rRelationshipId);
        return response;
    }
}
