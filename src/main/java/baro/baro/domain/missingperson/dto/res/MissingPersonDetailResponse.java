package baro.baro.domain.missingperson.dto.res;

import baro.baro.domain.missingperson.entity.MissingPerson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "실종자 상세 응답")
public class MissingPersonDetailResponse {
    @Schema(description = "실종자 ID", example = "1")
    private Long missingPersonId;

    @Schema(description = "실종자 이름", example = "김실종")
    private String name;

    @Schema(description = "생년월일", example = "2015-09-12")
    private String birthDate;

    @Schema(description = "주소", example = "대한민국 부산광역시 사상구 삼락동 29-6")
    private String address;

    @Schema(description = "실종 일시", example = "2024-12-06T00:00:00+09:00")
    private String missingDate;

    @Schema(description = "키 (cm)", example = "111")
    private Integer height;

    @Schema(description = "몸무게 (kg)", example = "28")
    private Integer weight;

    @Schema(description = "체형", example = "통통한 체형")
    private String body;

    @Schema(description = "기타 신체 특징", example = "왼쪽 팔에 점이 있음")
    private String bodyEtc;

    @Schema(description = "상의", example = "파란색 티셔츠")
    private String clothesTop;

    @Schema(description = "하의", example = "검은색 바지")
    private String clothesBottom;

    @Schema(description = "기타 의류", example = "빨간 모자")
    private String clothesEtc;

    @Schema(description = "위도", example = "35.188884")
    private Double latitude;
    
    @Schema(description = "경도", example = "128.903480")
    private Double longitude;

    @Schema(description = "사진 URL", example = "https://example.com/photo.jpg")
    private String photoUrl;

    @Schema(description = "AI 생성 성장/노화 예측 이미지 URL", example = "https://example.com/predicted.jpg")
    private String predictedFaceUrl;

    @Schema(description = "AI 생성 인상착의 기반 전신 이미지 URL", example = "https://example.com/appearance.jpg")
    private String appearanceImageUrl;

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
        response.photoUrl = missingPerson.getPhotoUrl();
        response.predictedFaceUrl = missingPerson.getPredictedFaceUrl();
        response.appearanceImageUrl = missingPerson.getAppearanceImageUrl();
        return response;
    }

    public static MissingPersonDetailResponse create(
            Long missingPersonId, String name, String birthDate, String address,
            String missingDate, Integer height, Integer weight, String body,
            String bodyEtc, String clothesTop, String clothesBottom,
            String clothesEtc, Double latitude, Double longitude, String photoUrl,
            String predictedFaceUrl, String appearanceImageUrl) {
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
        response.predictedFaceUrl = predictedFaceUrl;
        response.appearanceImageUrl = appearanceImageUrl;
        return response;
    }
}

