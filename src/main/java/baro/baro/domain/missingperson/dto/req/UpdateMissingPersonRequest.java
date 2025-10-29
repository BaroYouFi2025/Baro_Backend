package baro.baro.domain.missingperson.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "실종자 정보 수정 요청")
@Data
public class UpdateMissingPersonRequest {
    @Schema(description = "실종자 이름", example = "홍길동")
    private String name;

    @Schema(description = "생년월일 (ISO 8601 형식)", example = "2010-05-15")
    private String birthDate;

    @Schema(description = "실종자 사진 URL", example = "https://example.com/photo.jpg")
    private String photoUrl;

    @Schema(description = "실종 일시 (ISO 8601 형식)", example = "2024-01-15T14:30:00")
    private String missingDate;

    @Schema(description = "키 (cm)", example = "165")
    private Integer height;

    @Schema(description = "몸무게 (kg)", example = "55")
    private Integer weight;

    @Schema(description = "체형 설명", example = "마른 편")
    private String body;

    @Schema(description = "체형 기타 특징", example = "왼쪽 팔에 점이 있음")
    private String bodyEtc;

    @Schema(description = "상의 설명", example = "파란색 후드티")
    private String clothesTop;

    @Schema(description = "하의 설명", example = "검은색 청바지")
    private String clothesBottom;

    @Schema(description = "의류 기타 특징", example = "빨간색 운동화")
    private String clothesEtc;

    @Schema(description = "실종 위치 위도", example = "37.5665")
    private Double latitude;

    @Schema(description = "실종 위치 경도", example = "126.9780")
    private Double longitude;
    
    public static UpdateMissingPersonRequest create(
            String name, String birthDate, String photoUrl, String missingDate,
            Integer height, Integer weight, String body, String bodyEtc,
            String clothesTop, String clothesBottom, String clothesEtc,
            Double latitude, Double longitude) {
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
        request.latitude = latitude;
        request.longitude = longitude;
        return request;
    }
}
