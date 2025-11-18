package baro.baro.domain.policeoffice.dto.res;

import baro.baro.domain.policeoffice.entity.PoliceOffice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

@Getter
@Builder
@Schema(description = "경찰관서 정보 응답")
public class PoliceOfficeResponse {

    @Schema(description = "관서 ID (CSV 연번)", example = "1")
    private Long id;

    @Schema(description = "시도경찰청", example = "서울청")
    private String headquarters;

    @Schema(description = "관할 경찰서", example = "서울중부")
    private String station;

    @Schema(description = "관서명", example = "을지")
    private String officeName;

    @Schema(description = "구분", example = "지구대")
    private String officeType;

    @Schema(description = "대표 전화번호", example = "02-2279-1908")
    private String phoneNumber;

    @Schema(description = "주소", example = "서울특별시 중구 퇴계로49길 13")
    private String address;

    @Schema(description = "위도", example = "37.5665")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    private Double longitude;

    @Schema(description = "사용자 위치로부터의 거리 (km)", example = "1.2")
    private Double distanceKm;

    public static PoliceOfficeResponse from(PoliceOffice office, Double distanceKm) {
        Point point = office.getLocation();
        Double lat = point != null ? point.getY() : null;
        Double lon = point != null ? point.getX() : null;

        return PoliceOfficeResponse.builder()
                .id(office.getId())
                .headquarters(office.getHeadquarters())
                .station(office.getStation())
                .officeName(office.getOfficeName())
                .officeType(office.getOfficeType())
                .phoneNumber(office.getPhoneNumber())
                .address(office.getAddress())
                .latitude(lat)
                .longitude(lon)
                .distanceKm(distanceKm)
                .build();
    }
}
