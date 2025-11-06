package baro.baro.domain.missingperson.dto.res;

import baro.baro.domain.missingperson.entity.Sighting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * 실종자 발견 신고 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "실종자 발견 신고 응답")
public class ReportSightingResponse {
    
    @Schema(description = "신고 ID", example = "1")
    private Long sightingId;
    
    @Schema(description = "실종 케이스 ID", example = "1")
    private Long missingCaseId;
    
    @Schema(description = "실종자 ID", example = "1")
    private Long missingPersonId;
    
    @Schema(description = "실종자 이름", example = "아트주")
    private String missingPersonName;
    
    @Schema(description = "신고자 이름", example = "김현호")
    private String reporterName;
    
    @Schema(description = "발견 위치 위도", example = "37.5665")
    private Double latitude;
    
    @Schema(description = "발견 위치 경도", example = "126.9780")
    private Double longitude;
    
    @Schema(description = "발견 위치 주소", example = "서울특별시 중구 세종대로 지하 2")
    private String address;
    
    @Schema(description = "발견 상황 설명", example = "서울역 근처에서 비슷한 사람을 목격했습니다.")
    private String description;
    
    @Schema(description = "신고 일시", example = "2025-11-04T09:41:00+09:00")
    private ZonedDateTime reportedAt;
    
    @Schema(description = "신고 처리 메시지", example = "신고가 접수되었습니다. 실종자 등록자에게 알림이 전송되었습니다.")
    private String message;
    
    /**
     * Sighting 엔티티로부터 응답 DTO 생성
     */
    public static ReportSightingResponse from(Sighting sighting) {
        return ReportSightingResponse.builder()
                .sightingId(sighting.getId())
                .missingCaseId(sighting.getMissingCase().getId())
                .missingPersonId(sighting.getMissingCase().getMissingPerson().getId())
                .missingPersonName(sighting.getMissingCase().getMissingPerson().getName())
                .reporterName(sighting.getReporter().getName())
                .latitude(sighting.getLatitude())
                .longitude(sighting.getLongitude())
                .address(sighting.getAddress())
                .description(sighting.getDescription())
                .reportedAt(sighting.getCreatedAt())
                .message("신고가 접수되었습니다. 실종자 등록자에게 알림이 전송되었습니다.")
                .build();
    }
    
    /**
     * 성공 메시지와 함께 응답 생성
     */
    public static ReportSightingResponse create(
            Sighting sighting,
            String customMessage) {
        
        ReportSightingResponse response = from(sighting);
        return ReportSightingResponse.builder()
                .sightingId(response.getSightingId())
                .missingCaseId(response.getMissingCaseId())
                .missingPersonId(response.getMissingPersonId())
                .missingPersonName(response.getMissingPersonName())
                .reporterName(response.getReporterName())
                .latitude(response.getLatitude())
                .longitude(response.getLongitude())
                .address(response.getAddress())
                .description(response.getDescription())
                .reportedAt(response.getReportedAt())
                .message(customMessage)
                .build();
    }
}

