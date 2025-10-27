package baro.baro.domain.missingperson.dto.res;

import baro.baro.domain.missingperson.entity.MissingPerson;
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

    private Double latitude;
    private Double longitude;

    private String photoUrl;

    public static MissingPersonDetailResponse from(MissingPerson missingPerson) {
        MissingPersonDetailResponse response = new MissingPersonDetailResponse();
        response.missingPersonId = missingPerson.getId();
        response.name = missingPerson.getName();
        response.birthDate = missingPerson.getBirthDate() != null
                ? missingPerson.getBirthDate().toString()
                : null;
        response.address = missingPerson.getAddress();
        response.missingDate = missingPerson.getMissingDate() != null
                ? missingPerson.getMissingDate().toString()
                : null;
        response.height = missingPerson.getHeight();
        response.weight = missingPerson.getWeight();
        response.body = missingPerson.getBody();
        response.bodyEtc = missingPerson.getBodyEtc();
        response.clothesTop = missingPerson.getClothesTop();
        response.clothesBottom = missingPerson.getClothesBottom();
        response.clothesEtc = missingPerson.getClothesEtc();
        response.latitude = missingPerson.getLatitude();
        response.longitude = missingPerson.getLongitude();
        response.photoUrl = missingPerson.getPhotoUrl(); // TODO: PersonMedia 엔티티와 연결하여 photo_url 가져오기
        return response;
    }

    public static MissingPersonDetailResponse create(
            Long missingPersonId, String name, String birthDate, String address,
            String missingDate, Integer height, Integer weight, String body,
            String bodyEtc, String clothesTop, String clothesBottom,
            String clothesEtc, Double latitude, Double longitude, String photoUrl) {
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
        response.latitude = latitude;
        response.longitude = longitude;
        response.photoUrl = photoUrl;
        return response;
    }
}

