package baro.baro.domain.missingperson.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MissingPersonDetailResponse {
    @JsonProperty("missingPersonId")
    private Long missingPersonId;
    
    private String name;
    
    @JsonProperty("birth_date")
    private String birthDate;
    
    private String address;
    
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
    
    @JsonProperty("photo_url")
    private String photoUrl;
    
    public static MissingPersonDetailResponse create(
            Long missingPersonId, String name, String birthDate, String address,
            String missingDate, Integer height, Integer weight, String body,
            String bodyEtc, String clothesTop, String clothesBottom,
            String clothesEtc, String location, String photoUrl) {
        MissingPersonDetailResponse response = new MissingPersonDetailResponse();
        response.missingPersonId = missingPersonId;
        response.name = name;
        response.birthDate = birthDate;
        response.address = address;
        response.missingDate = missingDate;
        response.height = height;
        response.weight = weight;
        response.body = body;
        response.bodyEtc = bodyEtc;
        response.clothesTop = clothesTop;
        response.clothesBottom = clothesBottom;
        response.clothesEtc = clothesEtc;
        response.location = location;
        response.photoUrl = photoUrl;
        return response;
    }
}

