package baro.baro.domain.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "사용자 프로필 응답")
@Getter
@Builder
public class UserProfileResponse {
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    
    @Schema(description = "레벨", example = "1")
    private Integer level;
    
    @Schema(description = "경험치", example = "100")
    private Integer exp;
    
    @Schema(description = "칭호", example = "수색 초보자")
    private String title;
    
    @Schema(description = "프로필 사진 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;
    
    @Schema(description = "프로필 배경 색상", example = "#FFFFFF")
    private String profileBackgroundColor;
}