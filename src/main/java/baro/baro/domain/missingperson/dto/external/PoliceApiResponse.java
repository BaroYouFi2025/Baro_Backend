package baro.baro.domain.missingperson.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// 경찰청 실종자 API 응답 DTO
@Data
public class PoliceApiResponse {

    private Integer totalCount;

    private List<PoliceApiMissingPerson> list;

    private String result;

    private String msg;

}
