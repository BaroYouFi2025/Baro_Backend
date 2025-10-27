package baro.baro.domain.missingperson.dto.res;

import lombok.Data;

@Data
public class MissingPersonDetailResponse {
    private Long missingPersonId;
    
    private String name;

    private String birthDate;
    
    private String address;

    private String missingDate;
    
    private Integer height;
    
    private Integer weight;
    
    private String body;

    private String bodyEtc;

    private String clothesTop;

    private String clothesBottom;

    private String clothesEtc;

    private String location;

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

