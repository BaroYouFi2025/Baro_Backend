package baro.baro.domain.missingperson.dto.req;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterMissingPersonRequest {
    private String name;
    private Integer age;
    private String gender;
    private String description;
    private String location;
    private String lastSeenDate;
}
