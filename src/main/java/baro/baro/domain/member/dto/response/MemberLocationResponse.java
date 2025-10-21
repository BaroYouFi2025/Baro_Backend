package baro.baro.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구성원 위치 정보 응답 DTO
 *
 * 사용자와 관계가 있는 구성원의 위치, 배터리, 거리 정보를 제공합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구성원 위치 정보 응답")
public class MemberLocationResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이름", example = "김실종")
    private String name;

    @Schema(description = "관계", example = "가족")
    private String relationship;

    @Schema(description = "배터리 잔량 (%)", example = "45")
    private Integer batteryLevel;

    @Schema(description = "거리 (km)", example = "0.1")
    private Double distance;

    @Schema(description = "위치 정보")
    private LocationInfo location;

    /**
     * 위치 정보 내부 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "위치 정보")
    public static class LocationInfo {

        @Schema(description = "위도", example = "35.1763")
        private Double latitude;

        @Schema(description = "경도", example = "128.9664")
        private Double longitude;
    }
}
