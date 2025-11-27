package baro.baro.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Schema(description = "회원 탈퇴 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserRequest {
    @Schema(description = "비밀번호 확인", example = "password123!")
    private String password;

    public static DeleteUserRequest create(String password) {
        DeleteUserRequest request = new DeleteUserRequest();
        request.password = password;
        return request;
    }
}
