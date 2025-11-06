package baro.baro.domain.missingperson.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoundReportRequest {
    @NotNull(message = "실종자 ID는 필수입니다.")
    private Long missingPersonId;
    
    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;
    
    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;
    
    private String location; // 주소 또는 위치 설명 (선택)
    
    private String description;
}


