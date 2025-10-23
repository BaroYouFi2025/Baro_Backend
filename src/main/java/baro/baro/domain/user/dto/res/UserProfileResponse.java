package baro.baro.domain.user.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long userId;
    private String name;
    private Integer level;
    private Integer exp;
    private String title;
}