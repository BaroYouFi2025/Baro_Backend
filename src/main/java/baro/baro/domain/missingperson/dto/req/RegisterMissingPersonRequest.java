package baro.baro.domain.missingperson.dto.req;

import baro.baro.domain.missingperson.entity.GenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterMissingPersonRequest {
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "생년월일은 필수입니다.")
    private String birthDate;

    private String gender;

    private String photoUrl;

    @NotBlank(message = "실종일은 필수입니다.")
    private String missingDate;

    @Min(value = 0, message = "키는 0 이상이어야 합니다.")
    private Integer height;

    @Min(value = 0, message = "몸무게는 0 이상이어야 합니다.")
    private Integer weight;

    private String body;

    private String bodyEtc;

    private String clothesTop;

    private String clothesBottom;

    private String clothesEtc;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;
    
    public static RegisterMissingPersonRequest create(
            String name, String birthDate, String photoUrl, String missingDate,
            Integer height, Integer weight, String body, String bodyEtc,
            String clothesTop, String clothesBottom, String clothesEtc,
            Double latitude, Double longitude, String gender) {
        RegisterMissingPersonRequest request = new RegisterMissingPersonRequest();
        request.name = name;
        request.birthDate = birthDate;
        request.photoUrl = photoUrl;
        request.missingDate = missingDate;
        request.gender = gender;
        request.height = height;
        request.weight = weight;
        request.body = body;
        request.bodyEtc = bodyEtc;
        request.clothesTop = clothesTop;
        request.clothesBottom = clothesBottom;
        request.clothesEtc = clothesEtc;
        request.latitude = latitude;
        request.longitude = longitude;
        return request;
    }
}
