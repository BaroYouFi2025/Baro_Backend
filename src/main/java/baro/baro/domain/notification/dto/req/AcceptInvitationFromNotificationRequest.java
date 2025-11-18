package baro.baro.domain.notification.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 알림을 통한 초대 수락 요청 DTO
//
// 알림에서 직접 초대를 수락할 때 사용됩니다.
@Schema(description = "알림을 통한 초대 수락 요청")
@Data
public class AcceptInvitationFromNotificationRequest {

    @Schema(description = "관계 (초대한 사람 입장에서의 관계, 예: 아버지, 어머니)", example = "아버지")
    @NotBlank(message = "관계는 필수입니다.")
    private String relation;
}
