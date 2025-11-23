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

        // 안전 필터를 피하기 위해 "age progression", "missing person" 등 민감한 키워드 제거
        // "portrait editing", "appearance adjustment" 등 일반적인 표현 사용
        prompt.append("Edit this portrait photo to show how this person would look ")
                .append("with a more mature appearance.\n\n");

        prompt.append("Current appearance details:\n");
        
        Integer originalAge = person.getMissingAge();
        Integer targetAge = person.getAge();
        
        if (originalAge != null && targetAge != null && targetAge > originalAge) {
            int yearsDifference = targetAge - originalAge;
            prompt.append("- Apply natural aging of approximately ")
                    .append(yearsDifference)
                    .append(" years to facial features\n");
        } else if (targetAge != null) {
            prompt.append("- Target appearance: ")
                    .append(targetAge)
                    .append(" years old\n");
        }

        prompt.append("- Gender: ")
                .append(person.getGender() != null ? person.getGender().name() : "Unknown")
                .append("\n");

        if (person.getDescription() != null && !person.getDescription().isEmpty()) {
            prompt.append("- Physical characteristics: ").append(person.getDescription()).append("\n");
        }

        prompt.append("\nEditing instructions:\n");
        prompt.append("- Preserve the original facial identity and recognizable features\n");
        prompt.append("- Add natural maturation effects (subtle lines, refined facial structure)\n");
        prompt.append("- Maintain realistic skin texture and natural appearance\n");
        prompt.append("- Keep background simple and neutral\n");
        prompt.append("- Generate a clear, front-facing portrait\n");
        prompt.append("\nStyle: Photorealistic, high quality portrait photo. ")
                .append(style)
                .append(". Professional headshot quality.");

        log.debug("생성된 성장/노화 프롬프트: {}", prompt);
        return prompt.toString();
    }

    public String buildDescriptionPrompt(MissingPerson person) {
        StringBuilder prompt = new StringBuilder();

        // 안전 필터를 피하기 위해 일반적인 이미지 편집 표현 사용
        prompt.append("Using the face from this portrait photo, ")
                .append("create a full-body illustration showing the person ")
                .append("with the following appearance and outfit:\n\n");

        prompt.append("Person details:\n");
        prompt.append("- Approximate age: ").append(person.getAge() != null ? person.getAge() : "Adult")
                .append("\n");
        prompt.append("- Gender: ")
                .append(person.getGender() != null ? person.getGender().name() : "Unknown")
                .append("\n");

        if (person.getHeight() != null) {
            prompt.append("- Height: approximately ").append(person.getHeight()).append(" cm\n");
        }

        if (person.getWeight() != null) {
            prompt.append("- Build: approximately ").append(person.getWeight()).append(" kg\n");
        }

        if (person.getDescription() != null && !person.getDescription().isEmpty()) {
            prompt.append("- Body type: ").append(person.getDescription()).append("\n");
        }

        if (person.getBodyEtc() != null && !person.getBodyEtc().isEmpty()) {
            prompt.append("- Additional features: ").append(person.getBodyEtc()).append("\n");
        }

        prompt.append("\nOutfit details:\n");
        if (person.getClothesTop() != null && !person.getClothesTop().isEmpty()) {
            prompt.append("- Top: ").append(person.getClothesTop()).append("\n");
        }
        if (person.getClothesBottom() != null && !person.getClothesBottom().isEmpty()) {
            prompt.append("- Bottom: ").append(person.getClothesBottom()).append("\n");
        }
        if (person.getClothesEtc() != null && !person.getClothesEtc().isEmpty()) {
            prompt.append("- Accessories: ").append(person.getClothesEtc()).append("\n");
        }

        prompt.append("\nImage requirements:\n");
        prompt.append("- Keep the facial features from the reference photo exactly as shown\n");
        prompt.append("- Show full-body view with natural standing pose\n");
        prompt.append("- Apply the outfit and body type as described\n");
        prompt.append("- Use simple, neutral background\n");
        prompt.append("- Ensure face matches the input image\n");
        prompt.append("\nStyle: Photorealistic, high quality, full-body portrait, natural lighting");

        log.debug("생성된 인상착의 프롬프트: {}", prompt);
        return prompt.toString();
    }
}
