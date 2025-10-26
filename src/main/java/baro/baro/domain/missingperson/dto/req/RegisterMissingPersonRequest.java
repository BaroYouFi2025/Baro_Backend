package baro.baro.domain.missingperson.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterMissingPersonRequest {
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
}
