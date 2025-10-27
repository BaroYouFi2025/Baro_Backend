package baro.baro.domain.missingperson.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMissingPersonRequest {
    private String name;
    
    @JsonProperty("birth_date")
    private String birthDate;
    
    @JsonProperty("photo_url")
    private String photoUrl;
    
    @JsonProperty("missing_date")
    private String missingDate;
    
    private Integer height;
    
    private Integer weight;
    
    private String body;
    
    @JsonProperty("bodyEtc")
    private String bodyEtc;
    
    @JsonProperty("clothesTop")
    private String clothesTop;
    
    @JsonProperty("clothesBottom")
    private String clothesBottom;
    
    @JsonProperty("clothesEtc")
    private String clothesEtc;
    
    private String location;
    
    public static UpdateMissingPersonRequest create(
            String name, String birthDate, String photoUrl, String missingDate,
            Integer height, Integer weight, String body, String bodyEtc,
            String clothesTop, String clothesBottom, String clothesEtc, String location) {
        UpdateMissingPersonRequest request = new UpdateMissingPersonRequest();
        request.name = name;
        request.birthDate = birthDate;
        request.photoUrl = photoUrl;
        request.missingDate = missingDate;
        request.height = height;
        request.weight = weight;
        request.body = body;
        request.bodyEtc = bodyEtc;
        request.clothesTop = clothesTop;
        request.clothesBottom = clothesBottom;
        request.clothesEtc = clothesEtc;
        request.location = location;
        return request;
    }
}
