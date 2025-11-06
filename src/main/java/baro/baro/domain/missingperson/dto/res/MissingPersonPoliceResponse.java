package baro.baro.domain.missingperson.dto.res;

import baro.baro.domain.missingperson.entity.MissingPersonPolice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema(description = "경찰청 실종자 데이터 응답")
@Data
public class MissingPersonPoliceResponse {

    @Schema(description = "실종자 고유 식별 코드 (경찰청 API ID)", example = "123456789")
    private Long id;

    @Schema(description = "발생일 (YYYYMMDD)", example = "20231215")
    private String occurrenceDate;

    @Schema(description = "착의사항", example = "검은색 청바지, 흰색 티셔츠")
    private String dress;

    @Schema(description = "현재나이", example = "25")
    private Integer ageNow;

    @Schema(description = "실종 당시 나이", example = "20")
    private Integer missingAge;

    @Schema(description = "실종자 상태 코드", example = "010")
    private String statusCode;

    @Schema(description = "성별", example = "남자")
    private String gender;

    @Schema(description = "기타 특징", example = "왼쪽 팔에 문신")
    private String specialFeatures;

    @Schema(description = "발생 장소", example = "서울특별시 강남구 역삼동")
    private String occurrenceAddress;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "얼굴 사진 URL", example = "http://localhost:8080/uploads/images/police_123456789_abc123.jpg")
    private String photoUrl;

    @Schema(description = "데이터 수집 시각", example = "2024-01-15T10:30:00.000+00:00")
    private Date collectedAt;

    @Schema(description = "생성일", example = "2024-01-15T10:30:00.000+00:00")
    private Date createdAt;

    @Schema(description = "수정일", example = "2024-01-15T10:30:00.000+00:00")
    private Date updatedAt;

    public static MissingPersonPoliceResponse from(MissingPersonPolice entity) {
        MissingPersonPoliceResponse response = new MissingPersonPoliceResponse();
        response.id = entity.getId();
        response.occurrenceDate = entity.getOccurrenceDate();
        response.dress = entity.getDress();
        response.ageNow = entity.getAgeNow();
        response.missingAge = entity.getMissingAge();
        response.statusCode = entity.getStatusCode();
        response.gender = entity.getGender();
        response.specialFeatures = entity.getSpecialFeatures();
        response.occurrenceAddress = entity.getOccurrenceAddress();
        response.name = entity.getName();
        response.photoUrl = entity.getPhotoUrl();
        response.collectedAt = entity.getCollectedAt();
        response.createdAt = entity.getCreatedAt();
        response.updatedAt = entity.getUpdatedAt();
        return response;
    }
}
