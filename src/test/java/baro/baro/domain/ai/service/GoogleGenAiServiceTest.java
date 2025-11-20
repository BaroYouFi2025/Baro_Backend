package baro.baro.domain.ai.service;

import baro.baro.domain.ai.client.GeminiApiClient;
import baro.baro.domain.ai.entity.AssetType;
import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.processing.ImageProcessingService;
import baro.baro.domain.ai.prompt.PromptGeneratorService;
import baro.baro.domain.ai.service.generator.AgeProgressionGenerationStrategy;
import baro.baro.domain.ai.service.generator.DescriptionImageGenerationStrategy;
import baro.baro.domain.common.monitoring.MetricsService;
import baro.baro.domain.image.service.ImageService;
import baro.baro.domain.missingperson.entity.MissingPerson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoogleGenAiServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private MetricsService metricsService;

    @Mock
    private GeminiApiClient geminiApiClient;

    @Mock
    private PromptGeneratorService promptGeneratorService;

    @Mock
    private ImageProcessingService imageProcessingService;

    private GoogleGenAiService googleGenAiService;

    @BeforeEach
    void setupMocks() {
        when(imageProcessingService.loadImageAsBase64(anyString())).thenReturn("base64");
        when(imageProcessingService.detectMimeType(anyString())).thenReturn("image/png");
        when(promptGeneratorService.buildAgeProgressionPrompt(any(MissingPerson.class), anyString()))
                .thenReturn("prompt");
        when(promptGeneratorService.buildDescriptionPrompt(any(MissingPerson.class)))
                .thenReturn("prompt");
        when(geminiApiClient.generateImage(anyString(), anyString(), anyString()))
                .thenReturn("image".getBytes(StandardCharsets.UTF_8));

        AtomicInteger counter = new AtomicInteger();
        when(imageService.saveImageFromBytes(any(byte[].class), anyString(), anyString()))
                .thenAnswer(invocation -> "http://cdn/" + counter.getAndIncrement());

        AgeProgressionGenerationStrategy ageStrategy = new AgeProgressionGenerationStrategy(
                geminiApiClient, imageProcessingService, promptGeneratorService, imageService, metricsService
        );
        DescriptionImageGenerationStrategy descriptionStrategy = new DescriptionImageGenerationStrategy(
                geminiApiClient, imageProcessingService, promptGeneratorService, imageService, metricsService
        );
        googleGenAiService = new GoogleGenAiService(List.of(ageStrategy, descriptionStrategy));
    }

    @Test
    void generateAgeProgressionImagesReturnsFourUrls() {
        MissingPerson missingPerson = MissingPerson.builder()
                .id(10L)
                .photoUrl("http://localhost/images/test.jpg")
                .build();

        List<String> imageUrls = googleGenAiService.generateImages(missingPerson, AssetType.AGE_PROGRESSION);

        assertThat(imageUrls).hasSize(4);
        assertThat(imageUrls).allMatch(url -> url.startsWith("http://cdn/"));
    }

    @Test
    void generateDescriptionImageThrowsWhenPhotoMissing() {
        MissingPerson missingPerson = MissingPerson.builder()
                .id(11L)
                .photoUrl("")
                .build();

        AiException exception = assertThrows(AiException.class,
                () -> googleGenAiService.generateImages(missingPerson, AssetType.GENERATED_IMAGE));

        assertThat(exception.getAiErrorCode()).isEqualTo(AiErrorCode.PHOTO_URL_REQUIRED);
    }

    @Test
    void generateImagesThrowsWhenAssetTypeIsUnsupported() {
        AgeProgressionGenerationStrategy ageStrategyOnly = new AgeProgressionGenerationStrategy(
                geminiApiClient, imageProcessingService, promptGeneratorService, imageService, metricsService
        );
        googleGenAiService = new GoogleGenAiService(List.of(ageStrategyOnly));

        MissingPerson missingPerson = MissingPerson.builder()
                .id(12L)
                .photoUrl("http://localhost/images/test.jpg")
                .build();

        AiException exception = assertThrows(AiException.class,
                () -> googleGenAiService.generateImages(missingPerson, AssetType.GENERATED_IMAGE));

        assertThat(exception.getAiErrorCode()).isEqualTo(AiErrorCode.INVALID_ASSET_TYPE);
    }
}
