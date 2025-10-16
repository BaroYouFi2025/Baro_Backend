package baro.baro.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "멤버 정보 응답")
@Data
public class MemberResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    
    @Schema(description = "관계 (예: 아들, 딸, 아버지, 어머니)", example = "아들")
    private String relationship;
    
    @Schema(description = "배터리 잔량", example = "85%")
    private String batteryLevel;
    
    @Schema(description = "현재 사용자와의 거리 (미터)", example = "1523.5")
    private Double distance;
    
    @Schema(description = "위도", example = "37.5665")
    private Double latitude;
    
    @Schema(description = "경도", example = "126.9780")
    private Double longitude;

    public static MemberResponse create(Long userId, String name, String relationship, String batteryLevel, Double distance, Double latitude, Double longitude) {
        MemberResponse response = new MemberResponse();
        response.userId = userId;
        response.name = name;
        response.relationship = relationship;
        response.batteryLevel = batteryLevel;
        response.distance = distance;
        response.latitude = latitude;
        response.longitude = longitude;
        return response;
    }
}
