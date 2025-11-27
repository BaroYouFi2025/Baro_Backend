package baro.baro.domain.device.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Schema(description = "GPS 위치 업데이트 응답")
public class GpsUpdateResponse {

    @Schema(description = "위도", example = "37.5665")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    private Double longitude;

    @Schema(description = "기록 시간", example = "2025-10-20T12:00:00")
    private LocalDateTime recordedAt;

    @Schema(description = "성공 메시지", example = "GPS 위치가 업데이트되었습니다.")
    private String message;
}
