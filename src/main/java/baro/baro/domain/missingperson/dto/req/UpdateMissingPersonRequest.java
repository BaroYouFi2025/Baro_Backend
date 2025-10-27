package baro.baro.domain.missingperson.dto.req;

import lombok.Data;

@Data
public class UpdateMissingPersonRequest {
    private String name;

    private String birthDate;

    private String photoUrl;

    private String missingDate;
    
    private Integer height;
    
    private Integer weight;
    
    private String body;

    private String bodyEtc;

    private String clothesTop;

    private String clothesBottom;

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
