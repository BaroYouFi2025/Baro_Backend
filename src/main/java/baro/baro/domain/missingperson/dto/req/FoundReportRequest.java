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
    
    @NotNull(message = "위치는 필수입니다.")
    private String location;
    
    private String description;
}

