package baro.baro.domain.member.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "멤버 초대 요청")
@Data
public class InvitationRequest {
    @Schema(description = "초대할 사용자 ID", example = "1")
    private Long inviteeUserId;

    @Schema(description = "관계 (예: 아들, 딸, 아버지, 어머니)", example = "아들")
    private String relation;
}
