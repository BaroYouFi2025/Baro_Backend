package baro.baro.domain.ai.service.generator;

import baro.baro.domain.ai.client.GeminiApiClient;
import baro.baro.domain.ai.entity.AssetType;
import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.processing.ImageProcessingService;
import baro.baro.domain.ai.prompt.PromptGeneratorService;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.image.service.ImageService;
import baro.baro.domain.missingperson.entity.MissingPerson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.IntStream;

@Slf4j
@Component
public class AgeProgressionGenerationStrategy extends AbstractGoogleAiStrategy {

    private static final int REQUEST_COUNT = 4;
    private static final int MINIMUM_REQUIRED_IMAGES = 3;

    public AgeProgressionGenerationStrategy(GeminiApiClient geminiApiClient,
                                            ImageProcessingService imageProcessingService,
                                            PromptGeneratorService promptGeneratorService,
                                            ImageService imageService,
                                            MetricsService metricsService) {
        super(geminiApiClient, imageProcessingService, promptGeneratorService, imageService, metricsService);
    }

    @Override
    public boolean supports(AssetType assetType) {
        return assetType == AssetType.AGE_PROGRESSION;
    }

    @Override
    public List<String> generate(MissingPerson missingPerson) {
        validateMissingPerson(missingPerson);
        GenerationContext context = prepareGenerationContext(missingPerson);

        String prompt = promptGeneratorService.buildAgeProgressionPrompt(
                missingPerson,
                "Front-facing portrait, looking directly at camera"
        );

        List<CompletableFuture<String>> futures = IntStream.range(0, REQUEST_COUNT)
                .mapToObj(sequenceOrder ->
                        CompletableFuture.supplyAsync(() ->
                                generateAndPersist(context.base64Image(), context.mimeType(), prompt, sequenceOrder, AssetType.AGE_PROGRESSION)))
                .toList();

        List<String> imageUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<String> future = futures.get(i);
            try {
                imageUrls.add(future.join());
            } catch (CompletionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                errors.add(cause.getMessage());
                log.error("성장/노화 이미지 생성 실패 - Sequence: {}", i, cause);
            }
        }

        if (imageUrls.size() < MINIMUM_REQUIRED_IMAGES) {
            log.error("AI 이미지 생성 실패: 최소 {}장 필요하나 {}장만 성공. 실패 이유: {}",
                    MINIMUM_REQUIRED_IMAGES, imageUrls.size(), String.join(", ", errors));
            throw new AiException(AiErrorCode.INSUFFICIENT_IMAGES_GENERATED);
        }

        log.info("성장/노화 이미지 생성 완료 - 총 {}장 (요청: {}, 최소 요구: {})",
                imageUrls.size(), REQUEST_COUNT, MINIMUM_REQUIRED_IMAGES);
        return imageUrls;
    }
}
