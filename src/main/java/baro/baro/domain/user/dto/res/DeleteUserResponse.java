package baro.baro.domain.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "회원 탈퇴 응답")
public class DeleteUserResponse {
    @Schema(description = "응답 메시지", example = "회원 탈퇴가 완료되었습니다.")
    private String message;
    
    // DeleteUserResponse 생성 정적 팩토리 메서드
    public static DeleteUserResponse create(String message) {
        DeleteUserResponse response = new DeleteUserResponse();
        response.message = message;
        return response;
    }
}
