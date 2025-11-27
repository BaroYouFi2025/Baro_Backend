package baro.baro.domain.ai.prompt;

import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.entity.GenderType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptGeneratorServiceTest {

    private final PromptGeneratorService promptGeneratorService = new PromptGeneratorService();

    @Test
    void buildAgeProgressionPromptIncludesKeyAttributes() {
        MissingPerson person = MissingPerson.builder()
                .name("홍길동")
                .gender(GenderType.MALE)
                .body("Slim body type")
                .bodyEtc("Has a small scar above the eyebrow")
                .build();

        String prompt = promptGeneratorService.buildAgeProgressionPrompt(person);

        assertThat(prompt).contains("Gender: MALE");
        assertThat(prompt).contains("Slim body type");
        assertThat(prompt).contains("small scar above the eyebrow");
    }

    @Test
    void buildDescriptionPromptContainsClothingInfo() {
        MissingPerson person = MissingPerson.builder()
                .name("홍길동")
                .gender(GenderType.FEMALE)
                .height(165)
                .weight(54)
                .clothesTop("Blue jacket")
                .clothesBottom("Black jeans")
                .clothesEtc("White sneakers")
                .build();

        String prompt = promptGeneratorService.buildDescriptionPrompt(person);

        assertThat(prompt).contains("Blue jacket");
        assertThat(prompt).contains("Black jeans");
        assertThat(prompt).contains("White sneakers");
        assertThat(prompt).contains("full-body portrait");
    }
}
