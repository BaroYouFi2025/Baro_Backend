package baro.baro.domain.ai.service.generator;

import baro.baro.domain.ai.client.GeminiApiClient;
import baro.baro.domain.ai.entity.AssetType;
import baro.baro.domain.ai.processing.ImageProcessingService;
import baro.baro.domain.ai.prompt.PromptGeneratorService;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.image.service.ImageService;
import baro.baro.domain.missingperson.entity.MissingPerson;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DescriptionImageGenerationStrategy extends AbstractGoogleAiStrategy {

    public DescriptionImageGenerationStrategy(GeminiApiClient geminiApiClient,
                                              ImageProcessingService imageProcessingService,
                                              PromptGeneratorService promptGeneratorService,
                                              ImageService imageService,
                                              MetricsService metricsService) {
        super(geminiApiClient, imageProcessingService, promptGeneratorService, imageService, metricsService);
    }

    @Override
    public boolean supports(AssetType assetType) {
        return assetType == AssetType.GENERATED_IMAGE;
    }

    @Override
    public List<String> generate(MissingPerson missingPerson) {
        validateMissingPerson(missingPerson);
        GenerationContext context = prepareGenerationContext(missingPerson);
        String prompt = promptGeneratorService.buildDescriptionPrompt(missingPerson);
        String imageUrl = generateAndPersist(context.base64Image(), context.mimeType(), prompt, 0, AssetType.GENERATED_IMAGE);
        return List.of(imageUrl);
    }
}
