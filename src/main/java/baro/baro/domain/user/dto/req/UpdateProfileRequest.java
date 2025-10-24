package baro.baro.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Schema(description = "프로필 수정 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    
    @Schema(description = "칭호", example = "수색 초보자")
    private String title;
    
    @Schema(description = "프로필 사진 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;
    
    @Schema(description = "프로필 배경 색상 (HEX 코드)", example = "#FFFFFF")
    private String profileBackgroundColor;
}
