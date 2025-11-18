package baro.baro.domain.missingperson.dto.res;

import baro.baro.domain.missingperson.entity.Sighting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.ZonedDateTime;

// 발견 신고 상세 응답 DTO
//
// 알림을 통해 발견 신고 상세 정보를 조회할 때 사용됩니다.
@Schema(description = "발견 신고 상세 응답")
@Data
public class SightingDetailResponse {

    @Schema(description = "발견 신고 ID", example = "1")
    private Long sightingId;

    @Schema(description = "실종자 ID", example = "1")
    private Long missingPersonId;

    @Schema(description = "실종자 이름", example = "홍길동")
    private String missingPersonName;

    @Schema(description = "신고자 이름", example = "김철수")
    private String reporterName;

    @Schema(description = "발견 위치 위도", example = "37.5665")
    private Double latitude;

    @Schema(description = "발견 위치 경도", example = "126.9780")
    private Double longitude;

    @Schema(description = "발견 위치 주소", example = "서울특별시 중구 세종대로 110")
    private String address;

    @Schema(description = "신고 시간", example = "2025-01-15T10:30:00+09:00")
    private ZonedDateTime reportedAt;

    // Sighting 엔티티로부터 SightingDetailResponse를 생성합니다.
    //
    // @param sighting 발견 신고 엔티티
    // @return SightingDetailResponse
    public static SightingDetailResponse from(Sighting sighting) {
        SightingDetailResponse response = new SightingDetailResponse();
        response.sightingId = sighting.getId();
        response.missingPersonId = sighting.getMissingCase().getMissingPerson().getId();
        response.missingPersonName = sighting.getMissingCase().getMissingPerson().getName();
        response.reporterName = sighting.getReporter().getName();
        response.latitude = sighting.getLatitude();
        response.longitude = sighting.getLongitude();
        response.address = sighting.getAddress();
        response.reportedAt = sighting.getCreatedAt();
        return response;
    }
}
