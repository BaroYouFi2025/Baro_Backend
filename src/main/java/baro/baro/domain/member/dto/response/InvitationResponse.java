package baro.baro.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "멤버 초대 응답")
@Data
@AllArgsConstructor
public class InvitationResponse {
    @Schema(description = "생성된 초대 요청 ID", example = "1")
    private Long relationshipRequestId;
}
