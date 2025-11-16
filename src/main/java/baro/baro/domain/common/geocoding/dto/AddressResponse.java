package baro.baro.domain.common.geocoding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// 주소 변환 응답 DTO
@Data
@Schema(description = "주소 변환 응답")
public class AddressResponse {
    
    @Schema(description = "위도", example = "37.5665")
    private Double latitude;
    
    @Schema(description = "경도", example = "126.9780")
    private Double longitude;
    
    @Schema(description = "변환된 주소", example = "서울특별시 중구 세종대로 110")
    private String address;
    
    @Schema(description = "변환 성공 여부", example = "true")
    private Boolean success;
    
    public static AddressResponse create(Double latitude, Double longitude, String address, Boolean success) {
        AddressResponse response = new AddressResponse();
        response.latitude = latitude;
        response.longitude = longitude;
        response.address = address;
        response.success = success;
        return response;
    }
}
