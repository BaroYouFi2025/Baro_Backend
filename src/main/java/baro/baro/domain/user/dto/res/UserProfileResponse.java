package baro.baro.domain.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "사용자 프로필 응답")
@Data
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
    
    // UserProfileResponse 생성 정적 팩토리 메서드
    public static UserProfileResponse create(Long userId, String name, Integer level, Integer exp, 
                                             String title, String profileUrl, String profileBackgroundColor) {
        UserProfileResponse response = new UserProfileResponse();
        response.userId = userId;
        response.name = name;
        response.level = level;
        response.exp = exp;
        response.title = title;
        response.profileUrl = profileUrl;
        response.profileBackgroundColor = profileBackgroundColor;
        return response;
    }
    
    // User 엔티티로부터 생성
    public static UserProfileResponse from(baro.baro.domain.user.entity.User user) {
        return create(
            user.getId(),
            user.getName(),
            user.getLevel(),
            user.getExp(),
            user.getTitle(),
            user.getProfileUrl(),
            user.getProfileBackgroundColor()
        );
    }
}