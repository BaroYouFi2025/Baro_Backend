package baro.baro.domain.missingperson.dto.res;

import lombok.Data;

@Data
public class RegisterMissingPersonResponse {
    private Long missingPersonId;
    
    public static RegisterMissingPersonResponse create(Long missingPersonId) {
        RegisterMissingPersonResponse response = new RegisterMissingPersonResponse();
        response.missingPersonId = missingPersonId;
        return response;
    }
}
