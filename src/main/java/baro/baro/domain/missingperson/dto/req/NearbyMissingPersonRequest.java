package baro.baro.domain.missingperson.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyMissingPersonRequest {
    private Double latitude;
    private Double longitude;
    private Integer radius;
}
