package baro.baro.domain.ai.processing;

import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
@Slf4j
public class ImageProcessingService {

    @Value("${file.upload-dir:/uploads}")
    private String uploadDir;

    public String loadImageAsBase64(String photoUrl) {
        try {
            byte[] imageBytes;

            log.info("이미지 로드 시작 - URL: {}", photoUrl);
            Path localPath = convertUrlToLocalPath(photoUrl);

            if (localPath != null) {
                log.info("서버 로컬 파일에서 이미지 로드: {}", localPath);
                if (!Files.exists(localPath)) {
                    log.error("로컬 파일을 찾을 수 없음: {}", localPath);
                    throw new AiException(AiErrorCode.IMAGE_FILE_NOT_FOUND);
                }
                imageBytes = Files.readAllBytes(localPath);
            } else if (photoUrl.startsWith("http://") || photoUrl.startsWith("https://")) {
                log.info("외부 HTTP URL에서 이미지 로드: {}", photoUrl);
                URL url = new URL(photoUrl);
                try (InputStream inputStream = url.openStream()) {
                    imageBytes = StreamUtils.copyToByteArray(inputStream);
                }
            } else {
                Path absolutePath = Paths.get(photoUrl);
                log.info("로컬 파일에서 이미지 로드: {}", absolutePath);

                if (!Files.exists(absolutePath)) {
                    log.error("로컬 파일을 찾을 수 없음: {}", absolutePath);
                    throw new AiException(AiErrorCode.IMAGE_FILE_NOT_FOUND);
                }

                imageBytes = Files.readAllBytes(absolutePath);
            }

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            log.info("이미지 로드 완료 - 크기: {} bytes, Base64 길이: {}", imageBytes.length, base64.length());
            return base64;

        } catch (AiException e) {
            throw e;
        } catch (IOException e) {
            log.error("이미지 로드 실패 - URL: {}", photoUrl, e);
            throw new AiException(AiErrorCode.IMAGE_LOAD_FAILED);
        }
    }

    public String detectMimeType(String photoUrl) {
        String lowerUrl = photoUrl.toLowerCase();

        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.endsWith(".png")) {
            return "image/png";
        } else if (lowerUrl.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerUrl.endsWith(".webp")) {
            return "image/webp";
        } else {
            log.warn("알 수 없는 이미지 확장자 - 기본값(image/jpeg) 사용: {}", photoUrl);
            return "image/jpeg";
        }
    }

    public byte[] generatePlaceholderImage() {
        String base64Image = "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCXABmAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//2Q==";
        return Base64.getDecoder().decode(base64Image);
    }

    private Path convertUrlToLocalPath(String photoUrl) {
        try {
            if (!photoUrl.startsWith("http://") && !photoUrl.startsWith("https://")) {
                return null;
            }

            String path = photoUrl.substring(photoUrl.indexOf("/", 8));
            if (path.startsWith("/images/")) {
                String relativePath = path.substring(1);
                Path currentDir = Paths.get(System.getProperty("user.dir"));
                Path localPath = currentDir.resolve(uploadDir).resolve(relativePath);

                log.debug("URL → 로컬 경로 변환: {} → {}", photoUrl, localPath.toAbsolutePath());
                return localPath;
            }
            return null;

        } catch (Exception e) {
            log.warn("URL을 로컬 경로로 변환 실패: {}", photoUrl, e);
            return null;
        }
    }
}
