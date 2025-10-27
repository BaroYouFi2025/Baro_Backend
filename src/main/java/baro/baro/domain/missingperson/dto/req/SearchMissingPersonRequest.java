package baro.baro.domain.missingperson.dto.req;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchMissingPersonRequest {
    private Integer page = 0;
    
    private Integer size = 20;
    
    public static SearchMissingPersonRequest create(Integer page, Integer size) {
        SearchMissingPersonRequest request = new SearchMissingPersonRequest();
        request.page = page != null ? page : 0;
        request.size = size != null ? size : 20;
        return request;
    }
}
