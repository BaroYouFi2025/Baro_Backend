package baro.baro.domain.missingperson.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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
    
    public static MissingPersonResponse create(
            Long missingPersonId, String name, String address,
            String missingDate, Integer height, Integer weight, String body) {
        MissingPersonResponse response = new MissingPersonResponse();
        response.missingPersonId = missingPersonId;
        response.name = name;
        response.address = address;
        response.missingDate = missingDate;
        response.height = height;
        response.weight = weight;
        response.body = body;
        return response;
    }
}
