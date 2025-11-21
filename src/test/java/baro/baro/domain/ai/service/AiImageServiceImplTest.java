package baro.baro.domain.ai.service;

import baro.baro.domain.ai.dto.req.ApplyAiImageRequest;
import baro.baro.domain.ai.dto.req.GenerateAiImageRequest;
import baro.baro.domain.ai.dto.res.ApplyAiImageResponse;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.entity.AssetType;
import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.repository.AiAssetRepository;
import baro.baro.domain.missingperson.entity.MissingCase;
import baro.baro.domain.missingperson.entity.MissingPerson;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import baro.baro.domain.missingperson.repository.MissingCaseRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiImageServiceImplTest {

    @Mock
    private MissingCaseRepository missingCaseRepository;

    @Mock
    private AiAssetRepository aiAssetRepository;

    @Mock
    private GoogleGenAiService googleGenAiService;

    @InjectMocks
    private AiImageServiceImpl aiImageService;

    @Test
    void generateImageWithAgeProgressionSavesAllAssets() {
        MissingPerson missingPerson = createMissingPerson();
        MissingCase missingCase = createMissingCase(missingPerson);
        when(missingCaseRepository.findByMissingPersonId(1L)).thenReturn(Optional.of(missingCase));
        when(googleGenAiService.generateImages(missingPerson, AssetType.AGE_PROGRESSION))
                .thenReturn(List.of("url1", "url2", "url3"));
        when(aiAssetRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GenerateAiImageRequest request = GenerateAiImageRequest.create(1L, AssetType.AGE_PROGRESSION);

        try (MockedStatic<baro.baro.domain.common.util.SecurityUtil> mockedStatic = mockStatic(baro.baro.domain.common.util.SecurityUtil.class)) {
            mockedStatic.when(baro.baro.domain.common.util.SecurityUtil::getCurrentUser)
                    .thenReturn(missingCase.getReportedBy());

            GenerateAiImageResponse response = aiImageService.generateImage(request);

            assertThat(response.getImageUrls()).containsExactly("url1", "url2", "url3");
            verify(aiAssetRepository, times(3)).save(any());
        }
    }

    @Test
    void generateImageWithGeneratedImageUpdatesAppearanceUrl() {
        MissingPerson missingPerson = createMissingPerson();
        MissingCase missingCase = createMissingCase(missingPerson);
        when(missingCaseRepository.findByMissingPersonId(2L)).thenReturn(Optional.of(missingCase));
        when(googleGenAiService.generateImages(missingPerson, AssetType.GENERATED_IMAGE))
                .thenReturn(List.of("appearance-url"));
        when(aiAssetRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GenerateAiImageRequest request = GenerateAiImageRequest.create(2L, AssetType.GENERATED_IMAGE);

        try (MockedStatic<baro.baro.domain.common.util.SecurityUtil> mockedStatic = mockStatic(baro.baro.domain.common.util.SecurityUtil.class)) {
            mockedStatic.when(baro.baro.domain.common.util.SecurityUtil::getCurrentUser)
                    .thenReturn(missingCase.getReportedBy());

            GenerateAiImageResponse response = aiImageService.generateImage(request);

            assertThat(response.getImageUrls()).containsExactly("appearance-url");
            assertThat(missingPerson.getAppearanceImageUrl()).isEqualTo("appearance-url");
            verify(aiAssetRepository).save(any());
        }
    }

    @Test
    void applySelectedImageUpdatesPredictedUrlForAgeProgression() {
        MissingPerson missingPerson = createMissingPerson();
        MissingCase missingCase = createMissingCase(missingPerson);
        when(missingCaseRepository.findByMissingPersonId(3L)).thenReturn(Optional.of(missingCase));

        ApplyAiImageRequest request = ApplyAiImageRequest.create(3L, AssetType.AGE_PROGRESSION, "face-url");

        try (MockedStatic<baro.baro.domain.common.util.SecurityUtil> mockedStatic = mockStatic(baro.baro.domain.common.util.SecurityUtil.class)) {
            mockedStatic.when(baro.baro.domain.common.util.SecurityUtil::getCurrentUser)
                    .thenReturn(missingCase.getReportedBy());

            ApplyAiImageResponse response = aiImageService.applySelectedImage(request);

            assertThat(missingPerson.getPredictedFaceUrl()).isEqualTo("face-url");
            assertThat(response.getAppliedUrl()).isEqualTo("face-url");
        }
    }

    @Test
    void applySelectedImageThrowsWhenAssetTypeIsGeneratedImage() {
        ApplyAiImageRequest request = ApplyAiImageRequest.create(4L, AssetType.GENERATED_IMAGE, "ignored");

        try (MockedStatic<baro.baro.domain.common.util.SecurityUtil> mockedStatic = mockStatic(baro.baro.domain.common.util.SecurityUtil.class)) {
            mockedStatic.when(baro.baro.domain.common.util.SecurityUtil::getCurrentUser)
                    .thenReturn(createUser(10L));

            assertThatThrownBy(() -> aiImageService.applySelectedImage(request))
                    .isInstanceOf(AiException.class);
        }
    }

    @Test
    void generateImageThrowsWhenMissingCaseNotFound() {
        GenerateAiImageRequest request = GenerateAiImageRequest.create(5L, AssetType.AGE_PROGRESSION);
        when(missingCaseRepository.findByMissingPersonId(5L)).thenReturn(Optional.empty());

        try (MockedStatic<baro.baro.domain.common.util.SecurityUtil> mockedStatic = mockStatic(baro.baro.domain.common.util.SecurityUtil.class)) {
            mockedStatic.when(baro.baro.domain.common.util.SecurityUtil::getCurrentUser)
                    .thenReturn(createUser(20L));

            assertThatThrownBy(() -> aiImageService.generateImage(request))
                    .isInstanceOf(MissingPersonException.class);
        }
    }

    @Test
    void generateImageThrowsWhenMissingPersonEntityIsNull() {
        MissingCase missingCase = createMissingCase(null);
        GenerateAiImageRequest request = GenerateAiImageRequest.create(6L, AssetType.AGE_PROGRESSION);
        when(missingCaseRepository.findByMissingPersonId(6L)).thenReturn(Optional.of(missingCase));

        try (MockedStatic<baro.baro.domain.common.util.SecurityUtil> mockedStatic = mockStatic(baro.baro.domain.common.util.SecurityUtil.class)) {
            mockedStatic.when(baro.baro.domain.common.util.SecurityUtil::getCurrentUser)
                    .thenReturn(missingCase.getReportedBy());

            assertThatThrownBy(() -> aiImageService.generateImage(request))
                    .isInstanceOfSatisfying(AiException.class, e ->
                            assertThat(e.getAiErrorCode()).isEqualTo(AiErrorCode.MISSING_PERSON_NOT_FOUND));
        }
    }

    @Test
    void applySelectedImageThrowsWhenMissingCaseIsAbsent() {
        ApplyAiImageRequest request = ApplyAiImageRequest.create(8L, AssetType.AGE_PROGRESSION, "url");
        when(missingCaseRepository.findByMissingPersonId(8L)).thenReturn(Optional.empty());

        try (MockedStatic<baro.baro.domain.common.util.SecurityUtil> mockedStatic = mockStatic(baro.baro.domain.common.util.SecurityUtil.class)) {
            mockedStatic.when(baro.baro.domain.common.util.SecurityUtil::getCurrentUser)
                    .thenReturn(createUser(30L));

            assertThatThrownBy(() -> aiImageService.applySelectedImage(request))
                    .isInstanceOf(MissingPersonException.class);
        }
    }

    @Test
    void applySelectedImageThrowsWhenMissingPersonEntityIsNull() {
        MissingCase missingCase = createMissingCase(null);
        ApplyAiImageRequest request = ApplyAiImageRequest.create(9L, AssetType.AGE_PROGRESSION, "url");
        when(missingCaseRepository.findByMissingPersonId(9L)).thenReturn(Optional.of(missingCase));

        try (MockedStatic<baro.baro.domain.common.util.SecurityUtil> mockedStatic = mockStatic(baro.baro.domain.common.util.SecurityUtil.class)) {
            mockedStatic.when(baro.baro.domain.common.util.SecurityUtil::getCurrentUser)
                    .thenReturn(missingCase.getReportedBy());

            assertThatThrownBy(() -> aiImageService.applySelectedImage(request))
                    .isInstanceOfSatisfying(AiException.class, e ->
                            assertThat(e.getAiErrorCode()).isEqualTo(AiErrorCode.MISSING_PERSON_NOT_FOUND));
        }
    }

    private MissingPerson createMissingPerson() {
        MissingPerson person = MissingPerson.builder()
                .name("테스트")
                .birthDate(LocalDate.of(2000, 1, 1))
                .missingDate(LocalDateTime.now())
                .photoUrl("http://localhost/image.jpg")
                .build();
        ReflectionTestUtils.setField(person, "id", 30L);
        return person;
    }

    private MissingCase createMissingCase(MissingPerson missingPerson) {
        User owner = createUser(1L);
        MissingCase missingCase = MissingCase.builder()
                .missingPerson(missingPerson)
                .reportedBy(owner)
                .build();
        ReflectionTestUtils.setField(missingCase, "id", 40L);
        return missingCase;
    }

    private User createUser(Long id) {
        User user = User.builder()
                .uid("uid-" + id)
                .encodedPassword("encoded")
                .phoneE164("+8201000000000")
                .name("user" + id)
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "role", UserRole.USER);
        return user;
    }
}
