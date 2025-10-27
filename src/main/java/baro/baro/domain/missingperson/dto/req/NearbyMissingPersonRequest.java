package baro.baro.domain.missingperson.dto.req;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyMissingPersonRequest {
    private Double latitude;
    private Double longitude;
    private Integer radius;
    
    public static NearbyMissingPersonRequest create(Double latitude, Double longitude, Integer radius) {
        NearbyMissingPersonRequest request = new NearbyMissingPersonRequest();
        request.latitude = latitude;
        request.longitude = longitude;
        request.radius = radius;
        return request;
    }
}
