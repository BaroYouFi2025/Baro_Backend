package baro.baro.domain.missingperson.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "실종자 등록 응답")
public class RegisterMissingPersonResponse {
    @Schema(description = "실종자 ID", example = "1")
    private Long missingPersonId;
    
    public static RegisterMissingPersonResponse create(Long missingPersonId) {
        RegisterMissingPersonResponse response = new RegisterMissingPersonResponse();
        response.missingPersonId = missingPersonId;
        return response;
    }
}
