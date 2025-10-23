package baro.baro.domain.user.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteUserResponse {
    private String message;
}
