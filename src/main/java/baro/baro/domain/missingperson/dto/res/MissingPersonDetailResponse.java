package baro.baro.domain.missingperson.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
}

