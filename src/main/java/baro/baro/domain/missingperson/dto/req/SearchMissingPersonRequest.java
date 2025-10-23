package baro.baro.domain.missingperson.dto.req;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchMissingPersonRequest {
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 10;
}
