package baro.baro.domain.missingperson.dto.res;

import baro.baro.domain.missingperson.entity.MissingPerson;
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

    public static MissingPersonResponse from(MissingPerson missingPerson) {
        MissingPersonResponse response = new MissingPersonResponse();
        response.missingPersonId = missingPerson.getId();
        response.name = missingPerson.getName();
        response.address = missingPerson.getAddress();
        response.missingDate = missingPerson.getMissingDate() != null
                ? missingPerson.getMissingDate().toString()
                : null;
        response.height = missingPerson.getHeight();
        response.weight = missingPerson.getWeight();
        response.body = missingPerson.getBody();
        return response;
    }

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
