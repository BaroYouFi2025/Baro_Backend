package baro.baro.domain.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Schema(description = "사용자 검색 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRequest {
    @Schema(description = "사용자 ID (선택적, 비어있으면 사용자 주위 우선 조회)", example = "user4")
    private String uid;  // 선택적 - 비어있으면 전체 조회
    
    @Builder.Default
    @Schema(description = "페이지 번호", example = "0", defaultValue = "0")
    private Integer page = 0;
    
    @Builder.Default
    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    private Integer size = 20;
}

