package baro.baro.domain.ai.prompt;

import baro.baro.domain.missingperson.entity.MissingPerson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PromptGeneratorService {

    private static final String DEFAULT_STYLE =
            "Front-facing portrait, looking directly at camera";

    public String buildAgeProgressionPrompt(MissingPerson person) {
        return buildAgeProgressionPrompt(person, DEFAULT_STYLE);
    }

    public String buildAgeProgressionPrompt(MissingPerson person, String style) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Using the provided image of a person's face, ")
                .append("perform age progression to show how they would look now.\n\n");
        prompt.append("Age progression details:\n");
        prompt.append("- Original age (when photo was taken): ")
                .append(person.getMissingAge() != null ? person.getMissingAge() : "unknown")
                .append(" years old\n");
        prompt.append("- Target age (current age): ")
                .append(person.getAge() != null ? person.getAge() : "unknown")
                .append(" years old\n");
        prompt.append("- Gender: ")
                .append(person.getGender() != null ? person.getGender().name() : "Unknown")
                .append("\n");

        if (person.getDescription() != null && !person.getDescription().isEmpty()) {
            prompt.append("- Physical description: ").append(person.getDescription()).append("\n");
        }

        if (person.getBodyEtc() != null && !person.getBodyEtc().isEmpty()) {
            prompt.append("- Additional features: ").append(person.getBodyEtc()).append("\n");
        }

        prompt.append("\nInstructions:\n");
        prompt.append("- Keep the facial identity and core features recognizable\n");
        prompt.append("- Add natural aging effects appropriate for the age difference\n");
        prompt.append("- Maintain realistic skin texture, wrinkles, and facial structure changes\n");
        prompt.append("- Keep the background simple and neutral\n");
        prompt.append("- Generate a clear, front-facing portrait photo\n");
        prompt.append("\nStyle: Photorealistic, high quality portrait. ")
                .append(style)
                .append(". Professional headshot quality.");

        log.debug("생성된 성장/노화 프롬프트: {}", prompt);
        return prompt.toString();
    }

    public String buildDescriptionPrompt(MissingPerson person) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Using the provided image of a person's face, ")
                .append("generate a full-body portrait showing this person with the ")
                .append("following appearance and clothing:\n\n");
        prompt.append("Person information:\n");
        prompt.append("- Age: ").append(person.getAge() != null ? person.getAge() : "Unknown")
                .append(" years old\n");
        prompt.append("- Gender: ")
                .append(person.getGender() != null ? person.getGender().name() : "Unknown")
                .append("\n");

        if (person.getHeight() != null) {
            prompt.append("- Height: approximately ").append(person.getHeight()).append(" cm\n");
        }

        if (person.getWeight() != null) {
            prompt.append("- Weight: approximately ").append(person.getWeight()).append(" kg\n");
        }

        if (person.getDescription() != null && !person.getDescription().isEmpty()) {
            prompt.append("- Body type: ").append(person.getDescription()).append("\n");
        }

        if (person.getBodyEtc() != null && !person.getBodyEtc().isEmpty()) {
            prompt.append("- Additional physical features: ").append(person.getBodyEtc()).append("\n");
        }

        prompt.append("\nClothing details:\n");
        if (person.getClothesTop() != null && !person.getClothesTop().isEmpty()) {
            prompt.append("- Top: ").append(person.getClothesTop()).append("\n");
        }
        if (person.getClothesBottom() != null && !person.getClothesBottom().isEmpty()) {
            prompt.append("- Bottom: ").append(person.getClothesBottom()).append("\n");
        }
        if (person.getClothesEtc() != null && !person.getClothesEtc().isEmpty()) {
            prompt.append("- Accessories/Other: ").append(person.getClothesEtc()).append("\n");
        }

        prompt.append("\nInstructions:\n");
        prompt.append("- Keep the facial features from the provided image EXACTLY as they are\n");
        prompt.append("- Generate a full-body portrait showing the person standing naturally\n");
        prompt.append("- Apply the clothing and body type described above\n");
        prompt.append("- Use a simple, neutral background\n");
        prompt.append("- Ensure the face matches the input image perfectly\n");
        prompt.append("\nStyle: Photorealistic, high quality, full-body portrait, natural lighting");

        log.debug("생성된 인상착의 프롬프트: {}", prompt);
        return prompt.toString();
    }
}
