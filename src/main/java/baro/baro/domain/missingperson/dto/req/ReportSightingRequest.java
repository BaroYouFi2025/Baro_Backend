package baro.baro.domain.missingperson.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실종자 발견 신고 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실종자 발견 신고 요청")
public class ReportSightingRequest {
    
    @NotNull(message = "실종자 ID는 필수입니다.")
    @Schema(description = "실종자 ID", example = "1", required = true)
    private Long missingPersonId;
    
    @NotNull(message = "위도는 필수입니다.")
    @Schema(description = "발견 위치 위도", example = "37.5665", required = true)
    private Double latitude;
    
    @NotNull(message = "경도는 필수입니다.")
    @Schema(description = "발견 위치 경도", example = "126.9780", required = true)
    private Double longitude;
    
    @Schema(description = "발견 상황 설명", example = "서울역 근처에서 비슷한 사람을 목격했습니다.")
    private String description;
}

