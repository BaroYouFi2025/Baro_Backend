package baro.baro.domain.missingperson.dto.res;

import lombok.Data;

@Data
public class MissingPersonPoliceResponse {
    private Long missingPersonPoliceId;
    private String name;
    private String address;
    private String dress;
}
