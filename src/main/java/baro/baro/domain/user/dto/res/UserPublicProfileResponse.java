package baro.baro.domain.user.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPublicProfileResponse {
    private String uid;
    
    private String name;
    
    @JsonProperty("profile_url")
    private String profileUrl;
    
    @JsonProperty("profile_background_color")
    private String profileBackgroundColor;
}

