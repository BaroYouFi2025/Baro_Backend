package baro.baro.domain.user.dto.res;

import lombok.Data;

@Data
public class DeleteUserResponse {
    private String message;
    
    /**
     * DeleteUserResponse 생성 정적 팩토리 메서드
     */
    public static DeleteUserResponse create(String message) {
        DeleteUserResponse response = new DeleteUserResponse();
        response.message = message;
        return response;
    }
}
