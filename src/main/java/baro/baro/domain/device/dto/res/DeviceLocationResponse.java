package baro.baro.domain.device.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "기기 위치 조회 응답")
public class DeviceLocationResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "위치 정보")
    private LocationInfo location;

    @Schema(description = "위치 기록 시간", example = "2025-10-21T19:30:00")
    private LocalDateTime recordedAt;
    
    public static DeviceLocationResponse create(Long userId, LocationInfo location, LocalDateTime recordedAt) {
        DeviceLocationResponse response = new DeviceLocationResponse(userId, location, recordedAt);
        return response;
    }

    // 위치 정보 내부 클래스
    @Data
    @AllArgsConstructor
    @Schema(description = "위치 정보")
    public static class LocationInfo {
        
        @Schema(description = "위도", example = "35.1763")
        private Double latitude;

        @Schema(description = "경도", example = "128.9664")
        private Double longitude;
        
        public static LocationInfo create(Double latitude, Double longitude) {
            LocationInfo info = new LocationInfo(latitude, longitude);
            return info;
        }
    }
}
