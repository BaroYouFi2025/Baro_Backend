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

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractGoogleAiStrategy implements AiImageGenerationStrategy {

    protected final GeminiApiClient geminiApiClient;
    protected final ImageProcessingService imageProcessingService;
    protected final PromptGeneratorService promptGeneratorService;
    protected final ImageService imageService;
    protected final MetricsService metricsService;

    protected void validateMissingPerson(MissingPerson missingPerson) {
        if (missingPerson == null) {
            throw new AiException(AiErrorCode.MISSING_PERSON_NOT_FOUND);
        }
        if (missingPerson.getPhotoUrl() == null || missingPerson.getPhotoUrl().isBlank()) {
            log.error("AI 이미지 생성 실패 - photo_url이 없습니다. MissingPerson ID: {}",
                    missingPerson.getId());
            throw new AiException(AiErrorCode.PHOTO_URL_REQUIRED);
        }
    }

    protected GenerationContext prepareGenerationContext(MissingPerson missingPerson) {
        String photoUrl = missingPerson.getPhotoUrl();
        String base64Image = imageProcessingService.loadImageAsBase64(photoUrl);
        String mimeType = imageProcessingService.detectMimeType(photoUrl);
        return new GenerationContext(base64Image, mimeType);
    }

    protected String generateAndPersist(String base64Image,
                                        String mimeType,
                                        String prompt,
                                        int sequenceOrder,
                                        AssetType assetType) {
        long startTime = System.currentTimeMillis();
        log.info("이미지 편집 요청 - Sequence: {}, AssetType: {}, MIME: {}, Prompt: {}",
                sequenceOrder, assetType.name(), mimeType, prompt.substring(0, Math.min(100, prompt.length())));

        try {
            byte[] imageData = geminiApiClient.generateImage(base64Image, mimeType, prompt);

            String filename = String.format("ai-generated-%s.png", UUID.randomUUID());
            String imageUrl = imageService.saveImageFromBytes(imageData, filename, "image/png");

            metricsService.recordAiImageGenerationSuccess(assetType.name());
            metricsService.recordAiGenerationDuration(System.currentTimeMillis() - startTime, assetType.name());
            log.info("이미지 편집 완료 - Sequence: {}, URL: {}", sequenceOrder, imageUrl);
            return imageUrl;

        } catch (AiException e) {
            metricsService.recordAiImageGenerationFailure(assetType.name(), e.getAiErrorCode().name());
            throw e;
        } catch (Exception e) {
            metricsService.recordAiImageGenerationFailure(assetType.name(), e.getClass().getSimpleName());
            log.error("이미지 편집 실패 - Sequence: {}", sequenceOrder, e);
            return generateFallbackImage(assetType, sequenceOrder);
        }
    }

    private String generateFallbackImage(AssetType assetType, int sequenceOrder) {
        try {
            byte[] fallbackImage = imageProcessingService.generatePlaceholderImage();
            String filename = String.format("ai-fallback-%s.jpg", UUID.randomUUID());
            String fallbackUrl = imageService.saveImageFromBytes(fallbackImage, filename, "image/jpeg");
            log.warn("Fallback 이미지 URL 반환 - Sequence: {}, URL: {}", sequenceOrder, fallbackUrl);
            return fallbackUrl;
        } catch (Exception fallbackException) {
            log.error("Fallback 이미지 생성도 실패 - Sequence: {}", sequenceOrder, fallbackException);
            throw new AiException(AiErrorCode.IMAGE_SAVE_FAILED);
        }
    }

    protected record GenerationContext(String base64Image, String mimeType) {}
}
