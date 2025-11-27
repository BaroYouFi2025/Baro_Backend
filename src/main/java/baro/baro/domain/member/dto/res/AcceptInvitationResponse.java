package baro.baro.domain.member.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "초대 수락 응답")
@Data
public class AcceptInvitationResponse {
    @Schema(description = "생성된 관계 ID 목록 (양방향 관계이므로 2개)", example = "[1, 2]")
    private List<Long> relationshipIds;

    static public AcceptInvitationResponse of(Long oRelationshipId, Long rRelationshipId) {
        AcceptInvitationResponse response = new AcceptInvitationResponse();
        response.relationshipIds = new ArrayList<>();
        response.relationshipIds.add(oRelationshipId);
        response.relationshipIds.add(rRelationshipId);
        return response;
    }
}
