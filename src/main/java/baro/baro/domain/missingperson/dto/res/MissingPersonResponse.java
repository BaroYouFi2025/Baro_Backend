package baro.baro.domain.missingperson.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class MissingPersonResponse {
    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private String description;
    private String location;
    private String address;
    private ZonedDateTime lastSeenDate;
}
