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
    
    @JsonProperty("missing_date")
    private String missingDate;
    
    private Integer height;
    
    private Integer weight;
    
    private String body;
}
