package baro.baro.domain.missingperson.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// 경찰청 실종자 API 개별 데이터 DTO
// API 응답 순서와 동일하게 필드 배치
@Data
public class PoliceApiMissingPerson {

    private Integer rnum;

    private String occrde; // 발생일

    private String alldressingDscd; // 착의사항

    private String ageNow; // 현재나이 (String으로 옴)

    private Integer age; // 실종 당시 나이

    private String writngTrgetDscd; // 실종자 상태 코드

    private String sexdstnDscd; // 성별

    private String etcSpfeatr; // 기타 특징

    private String occrAdres; // 발생 장소

    private String nm; // 이름

    private Long msspsnIdntfccd; // 고유 ID

    private Integer tknphotolength; // 인코딩 길이

    private String tknphotoFile; // Base64 인코딩된 이미지

}
