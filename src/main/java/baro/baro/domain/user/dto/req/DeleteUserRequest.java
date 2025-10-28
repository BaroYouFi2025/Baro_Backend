package baro.baro.domain.user.dto.req;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserRequest {
    private String password;
    
    public static DeleteUserRequest create(String password) {
        DeleteUserRequest request = new DeleteUserRequest();
        request.password = password;
        return request;
    }
}
