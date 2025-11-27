package baro.baro.domain.missingperson.dto.res;

import baro.baro.domain.missingperson.entity.MissingPerson;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "실종자 목록 응답")
public class MissingPersonResponse {

    @Schema(description = "실종자 ID", example = "1")
    private Long missingPersonId;

    @Schema(description = "실종자 이름", example = "김수호")
    private String name;

    @Schema(description = "주소", example = "대한민국 부산광역시 사상구 삼락동 29-6")
    private String address;

    @JsonProperty("missing_date")
    @Schema(description = "실종 일시", example = "2024-12-06")
    private String missingDate;

    @Schema(description = "키 (cm)", example = "111")
    private Integer height;

    @Schema(description = "몸무게 (kg)", example = "28")
    private Integer weight;

    @Schema(description = "체형", example = "통통한 체형")
    private String body;

    @Schema(description = "사진 URL", example = "https://example.com/photo.jpg")
    private String photoUrl;

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
        response.photoUrl = missingPerson.getPhotoUrl();
        return response;
    }

    public static MissingPersonResponse create(
            Long missingPersonId, String name, String address,
            String missingDate, Integer height, Integer weight, String body, String photoUrl) {
        MissingPersonResponse response = new MissingPersonResponse();
        response.missingPersonId = missingPersonId;
        response.name = name;
        response.address = address;
        response.missingDate = missingDate;
        response.height = height;
        response.weight = weight;
        response.body = body;
        response.photoUrl = photoUrl;
        return response;
    }
}
