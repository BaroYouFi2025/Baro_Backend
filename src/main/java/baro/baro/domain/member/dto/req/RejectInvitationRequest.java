package baro.baro.domain.member.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "초대 거절 요청")
@Data
public class RejectInvitationRequest {
    @Schema(description = "거절할 초대 ID", example = "1")
    private Long relationshipId;
}
