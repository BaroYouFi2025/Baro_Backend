package baro.baro.domain.ai.processing;

import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ImageProcessingService 테스트")
class ImageProcessingServiceTest {

    private ImageProcessingService imageProcessingService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageProcessingService = new ImageProcessingService();
        ReflectionTestUtils.setField(imageProcessingService, "uploadDir", tempDir.toString());
    }

    @Test
    @DisplayName("MIME 타입 감지 - JPEG")
    void detectMimeType_jpeg() {
        // given
        String photoUrl1 = "https://example.com/photo.jpg";
        String photoUrl2 = "https://example.com/photo.jpeg";
        String photoUrl3 = "/local/path/image.JPG";
        String photoUrl4 = "/local/path/image.JPEG";

        // when & then
        assertThat(imageProcessingService.detectMimeType(photoUrl1)).isEqualTo("image/jpeg");
        assertThat(imageProcessingService.detectMimeType(photoUrl2)).isEqualTo("image/jpeg");
        assertThat(imageProcessingService.detectMimeType(photoUrl3)).isEqualTo("image/jpeg");
        assertThat(imageProcessingService.detectMimeType(photoUrl4)).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("MIME 타입 감지 - PNG")
    void detectMimeType_png() {
        // given
        String photoUrl1 = "https://example.com/photo.png";
        String photoUrl2 = "/local/path/image.PNG";

        // when & then
        assertThat(imageProcessingService.detectMimeType(photoUrl1)).isEqualTo("image/png");
        assertThat(imageProcessingService.detectMimeType(photoUrl2)).isEqualTo("image/png");
    }

    @Test
    @DisplayName("MIME 타입 감지 - GIF")
    void detectMimeType_gif() {
        // given
        String photoUrl1 = "https://example.com/photo.gif";
        String photoUrl2 = "/local/path/image.GIF";

        // when & then
        assertThat(imageProcessingService.detectMimeType(photoUrl1)).isEqualTo("image/gif");
        assertThat(imageProcessingService.detectMimeType(photoUrl2)).isEqualTo("image/gif");
    }

    @Test
    @DisplayName("MIME 타입 감지 - WebP")
    void detectMimeType_webp() {
        // given
        String photoUrl1 = "https://example.com/photo.webp";
        String photoUrl2 = "/local/path/image.WEBP";

        // when & then
        assertThat(imageProcessingService.detectMimeType(photoUrl1)).isEqualTo("image/webp");
        assertThat(imageProcessingService.detectMimeType(photoUrl2)).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("MIME 타입 감지 - 알 수 없는 확장자는 기본값(image/jpeg) 반환")
    void detectMimeType_unknownExtension() {
        // given
        String photoUrl1 = "https://example.com/photo.bmp";
        String photoUrl2 = "https://example.com/photo.tiff";
        String photoUrl3 = "https://example.com/photo";

        // when & then
        assertThat(imageProcessingService.detectMimeType(photoUrl1)).isEqualTo("image/jpeg");
        assertThat(imageProcessingService.detectMimeType(photoUrl2)).isEqualTo("image/jpeg");
        assertThat(imageProcessingService.detectMimeType(photoUrl3)).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("플레이스홀더 이미지 생성 - Base64 디코딩 가능한 바이트 배열 반환")
    void generatePlaceholderImage() {
        // when
        byte[] placeholderImage = imageProcessingService.generatePlaceholderImage();

        // then
        assertThat(placeholderImage).isNotNull();
        assertThat(placeholderImage).isNotEmpty();

        // Base64로 다시 인코딩했을 때 유효한지 확인
        String reEncoded = Base64.getEncoder().encodeToString(placeholderImage);
        assertThat(reEncoded).isNotBlank();
    }

    @Test
    @DisplayName("로컬 파일에서 이미지 로드 성공 - Base64 반환")
    void loadImageAsBase64_localFile_success() throws IOException {
        // given
        Path testImagePath = tempDir.resolve("test-image.jpg");
        byte[] testImageBytes = "test image content".getBytes();
        Files.write(testImagePath, testImageBytes);

        // when
        String base64 = imageProcessingService.loadImageAsBase64(testImagePath.toString());

        // then
        assertThat(base64).isNotBlank();
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        assertThat(decodedBytes).isEqualTo(testImageBytes);
    }

    @Test
    @DisplayName("로컬 파일 없음 - IMAGE_FILE_NOT_FOUND 예외 발생")
    void loadImageAsBase64_localFile_notFound() {
        // given
        String nonExistentPath = tempDir.resolve("non-existent.jpg").toString();

        // when & then
        assertThatThrownBy(() -> imageProcessingService.loadImageAsBase64(nonExistentPath))
                .isInstanceOf(AiException.class)
                .hasFieldOrPropertyWithValue("aiErrorCode", AiErrorCode.IMAGE_FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("서버 로컬 경로에서 이미지 로드 성공 (/images/ 경로)")
    void loadImageAsBase64_serverLocalPath_success() throws IOException {
        // given
        Path imagesDir = tempDir.resolve("images");
        Files.createDirectories(imagesDir);
        Path testImagePath = imagesDir.resolve("test.jpg");
        byte[] testImageBytes = "server image content".getBytes();
        Files.write(testImagePath, testImageBytes);

        String serverUrl = "http://localhost:8080/images/test.jpg";

        // when
        String base64 = imageProcessingService.loadImageAsBase64(serverUrl);

        // then
        assertThat(base64).isNotBlank();
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        assertThat(decodedBytes).isEqualTo(testImageBytes);
    }

    @Test
    @DisplayName("서버 로컬 파일 없음 - IMAGE_FILE_NOT_FOUND 예외 발생")
    void loadImageAsBase64_serverLocalPath_notFound() {
        // given
        String serverUrl = "http://localhost:8080/images/non-existent.jpg";

        // when & then
        assertThatThrownBy(() -> imageProcessingService.loadImageAsBase64(serverUrl))
                .isInstanceOf(AiException.class)
                .hasFieldOrPropertyWithValue("aiErrorCode", AiErrorCode.IMAGE_FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("외부 HTTP URL에서 이미지 로드 실패 - IMAGE_LOAD_FAILED 예외 발생")
    void loadImageAsBase64_externalUrl_failed() {
        // given
        String invalidUrl = "http://invalid-domain-that-does-not-exist-12345.com/image.jpg";

        // when & then
        assertThatThrownBy(() -> imageProcessingService.loadImageAsBase64(invalidUrl))
                .isInstanceOf(AiException.class)
                .hasFieldOrPropertyWithValue("aiErrorCode", AiErrorCode.IMAGE_LOAD_FAILED);
    }

    @Test
    @DisplayName("잘못된 프로토콜 URL - IMAGE_FILE_NOT_FOUND 예외 발생 (로컬 파일로 취급됨)")
    void loadImageAsBase64_invalidProtocol() {
        // given
        String invalidUrl = "ftp://example.com/image.jpg";

        // when & then
        // FTP 프로토콜은 http/https가 아니므로 로컬 파일 경로로 취급되어 IMAGE_FILE_NOT_FOUND 발생
        assertThatThrownBy(() -> imageProcessingService.loadImageAsBase64(invalidUrl))
                .isInstanceOf(AiException.class)
                .hasFieldOrPropertyWithValue("aiErrorCode", AiErrorCode.IMAGE_FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("빈 파일 경로 - 예외 발생")
    void loadImageAsBase64_emptyPath() {
        // given
        String emptyPath = "";

        // when & then
        assertThatThrownBy(() -> imageProcessingService.loadImageAsBase64(emptyPath))
                .isInstanceOf(AiException.class);
    }

    @Test
    @DisplayName("다양한 이미지 포맷 로드 성공")
    void loadImageAsBase64_variousFormats() throws IOException {
        // given
        String[] formats = {"test.jpg", "test.jpeg", "test.png", "test.gif", "test.webp"};

        for (String format : formats) {
            Path imagePath = tempDir.resolve(format);
            byte[] imageBytes = ("content of " + format).getBytes();
            Files.write(imagePath, imageBytes);

            // when
            String base64 = imageProcessingService.loadImageAsBase64(imagePath.toString());

            // then
            assertThat(base64).isNotBlank();
            byte[] decodedBytes = Base64.getDecoder().decode(base64);
            assertThat(decodedBytes).isEqualTo(imageBytes);
        }
    }

    @Test
    @DisplayName("큰 이미지 파일 로드 성공")
    void loadImageAsBase64_largeFile() throws IOException {
        // given
        Path largeImagePath = tempDir.resolve("large-image.jpg");
        byte[] largeImageBytes = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeImageBytes.length; i++) {
            largeImageBytes[i] = (byte) (i % 256);
        }
        Files.write(largeImagePath, largeImageBytes);

        // when
        String base64 = imageProcessingService.loadImageAsBase64(largeImagePath.toString());

        // then
        assertThat(base64).isNotBlank();
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        assertThat(decodedBytes).hasSize(largeImageBytes.length);
    }

    @Test
    @DisplayName("Base64 인코딩 결과 검증")
    void loadImageAsBase64_validBase64Format() throws IOException {
        // given
        Path testImagePath = tempDir.resolve("test.jpg");
        byte[] testBytes = "Hello, World!".getBytes();
        Files.write(testImagePath, testBytes);

        // when
        String base64 = imageProcessingService.loadImageAsBase64(testImagePath.toString());

        // then
        assertThat(base64).isNotBlank();
        assertThat(base64).matches("^[A-Za-z0-9+/]+=*$"); // Base64 패턴 검증

        // 디코딩 가능 여부 확인
        assertThatCode(() -> Base64.getDecoder().decode(base64))
                .doesNotThrowAnyException();
    }
}
