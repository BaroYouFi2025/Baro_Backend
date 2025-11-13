package baro.baro.domain.user.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 공개 프로필 응답")
public class UserPublicProfileResponse {
    @Schema(description = "사용자 ID", example = "user4")
    private String uid;
    
    @Schema(description = "사용자 이름", example = "배성민")
    private String name;
    
    @JsonProperty("profile_url")
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile")
    private String profileUrl;
    
    @JsonProperty("profile_background_color")
    @Schema(description = "프로필 배경색", example = "#FFFFFF")
    private String profileBackgroundColor;
}

