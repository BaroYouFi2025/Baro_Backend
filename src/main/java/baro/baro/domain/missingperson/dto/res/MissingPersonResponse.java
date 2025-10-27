package baro.baro.domain.missingperson.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissingPersonResponse {
    @JsonProperty("missingPersonId")
    private Long missingPersonId;
    
    private String name;
    
    private String address;
    
    private String body;
    
    @JsonProperty("photo_url")
    private String photoUrl;
}
