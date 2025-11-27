package baro.baro.domain.missingperson.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Schema(description = "실종자 목록 검색 요청")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchMissingPersonRequest {
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page = 0;

    @Schema(description = "페이지 크기 (1-100)", example = "20", defaultValue = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
    private Integer size = 20;

    public static SearchMissingPersonRequest create(Integer page, Integer size) {
        SearchMissingPersonRequest request = new SearchMissingPersonRequest();
        request.page = page != null ? page : 0;
        request.size = size != null ? size : 20;
        return request;
    }
}
