package baro.baro.domain.image.service;

import baro.baro.domain.image.exception.ImageException;
import baro.baro.domain.image.repository.ImageRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.entity.UserRole;
import baro.baro.domain.common.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @TempDir
    Path tempDir;

    private ImageServiceImpl imageService;
    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        imageService = new ImageServiceImpl(imageRepository);
        ReflectionTestUtils.setField(imageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(imageService, "baseUrl", "http://localhost:8080");
        User mockUser = createTestUser();
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(mockUser);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    void saveImageFromBytesRejectsUnsupportedExtensions() {
        byte[] imageData = "fake".getBytes(StandardCharsets.UTF_8);

        assertThrows(ImageException.class,
            () -> imageService.saveImageFromBytes(imageData, "payload.exe", "image/png"));
    }

    @Test
    void saveImageFromBytesPersistsImageUnderUploadRoot() throws Exception {
        byte[] imageData = "fake".getBytes(StandardCharsets.UTF_8);

        String publicUrl = imageService.saveImageFromBytes(imageData, "payload.png", "image/png");

        assertTrue(publicUrl.startsWith("http://localhost:8080/images/ai/"));

        String relativePath = publicUrl.replace("http://localhost:8080", "");
        Path storedFile = Paths.get(tempDir.toString(), relativePath.substring(1));
        assertTrue(Files.exists(storedFile));
    }

    @Test
    void uploadImageRejectsEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThrows(ImageException.class, () -> imageService.uploadImage(empty));
    }

    @Test
    void uploadImageRejectsInvalidExtension() {
        MockMultipartFile svg = new MockMultipartFile("file", "image.svg", "image/svg+xml", "fake".getBytes());

        assertThrows(ImageException.class, () -> imageService.uploadImage(svg));
    }

    @Test
    void uploadImageStoresFileUnderImagesDirectory() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "fake".getBytes());

        String publicUrl = imageService.uploadImage(file);

        assertTrue(publicUrl.startsWith("http://localhost:8080/images/"));
        String relativePath = publicUrl.replace("http://localhost:8080", "");
        Path storedFile = Paths.get(tempDir.toString(), relativePath.substring(1));
        assertTrue(Files.exists(storedFile));
    }

    @Test
    void saveImageFromBytesRejectsOversizedPayload() {
        byte[] tooLarge = new byte[10 * 1024 * 1024 + 1];

        assertThrows(ImageException.class,
            () -> imageService.saveImageFromBytes(tooLarge, "big.png", "image/png"));
    }

    private User createTestUser() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "uid", "tester");
        ReflectionTestUtils.setField(user, "passwordHash", "password");
        ReflectionTestUtils.setField(user, "phoneE164", "+821012345678");
        ReflectionTestUtils.setField(user, "name", "테스터");
        ReflectionTestUtils.setField(user, "birthDate", java.time.LocalDate.of(1990, 1, 1));
        ReflectionTestUtils.setField(user, "role", UserRole.USER);
        ReflectionTestUtils.setField(user, "isActive", true);
        return user;
    }
}
