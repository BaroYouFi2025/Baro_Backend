package baro.baro.domain.member.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "초대 수락 요청")
@Data
@AllArgsConstructor
public class AcceptInvitationRequest {
    @Schema(description = "초대 ID", example = "1")
    private Long relationshipRequestId;

    @Schema(description = "관계 (초대한 사람 입장에서의 관계, 예: 아버지, 어머니)", example = "아버지")
    private String relation;
}
