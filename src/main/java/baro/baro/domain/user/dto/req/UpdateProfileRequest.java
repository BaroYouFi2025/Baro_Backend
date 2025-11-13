package baro.baro.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Schema(description = "프로필 수정 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Schema(description = "사용자 이름", example = "배성민")
    private String name;
    
    @Schema(description = "칭호", example = "수색 초보자")
    private String title;
    
    @Schema(description = "프로필 사진 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;
    
    @Schema(description = "프로필 배경 색상 (색)", example = "#FFFFFF")
    private String profileBackgroundColor;
    
    public static UpdateProfileRequest create(String name, String title, String profileUrl, String profileBackgroundColor) {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.name = name;
        request.title = title;
        request.profileUrl = profileUrl;
        request.profileBackgroundColor = profileBackgroundColor;
        return request;
    }
}
