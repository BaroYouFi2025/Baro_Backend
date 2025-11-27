package baro.baro.domain.image.service;

import baro.baro.domain.image.entity.Image;
import baro.baro.domain.image.exception.ImageErrorCode;
import baro.baro.domain.image.exception.ImageException;
import baro.baro.domain.image.repository.ImageRepository;
import baro.baro.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static baro.baro.domain.common.util.SecurityUtil.getCurrentUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS =
        Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final ImageRepository imageRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${file.upload.base-url}")
    private String baseUrl;

    @Override
    public String uploadImage(MultipartFile file) {
        User user = getCurrentUser();

        String extension = validateFile(file);
        String savedPath = saveFile(file, extension);

        Image image = Image.create(
            savedPath,
            file.getOriginalFilename(),
            file.getSize(),
            file.getContentType(),
            user
        );
        imageRepository.save(image);

        return baseUrl + savedPath;
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageException(ImageErrorCode.EMPTY_FILE);
        }

        validateMimeType(file.getContentType());
        validateFileSize(file.getSize());
        return resolveExtension(file.getOriginalFilename());
    }

    @Override
    public String saveImageFromBytes(byte[] imageData, String filename, String mimeType) {
        String extension = validateImageBytes(imageData, filename, mimeType);
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String savedPath = saveFileFromBytes(imageData, uniqueFilename);
        return baseUrl + savedPath;
    }

    private String validateImageBytes(byte[] imageData, String filename, String mimeType) {
        if (imageData == null || imageData.length == 0) {
            throw new ImageException(ImageErrorCode.EMPTY_FILE);
        }

        validateFileSize(imageData.length);
        validateMimeType(mimeType);
        return resolveExtension(filename);
    }

    private void validateMimeType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_TYPE);
        }
    }

    private void validateFileSize(long size) {
        if (size > MAX_FILE_SIZE) {
            throw new ImageException(ImageErrorCode.FILE_TOO_LARGE);
        }
    }

    private String resolveExtension(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_TYPE);
        }

        String extension = filename.substring(filename.lastIndexOf("."))
            .toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_TYPE);
        }
        return extension;
    }

    private String saveFile(MultipartFile file, String extension) {
        try {
            LocalDate now = LocalDate.now();
            String datePath = String.format("%d/%02d/%02d",
                now.getYear(), now.getMonth().getValue(), now.getDayOfMonth());

            String relativeDirectory = "images/" + datePath;
            Path directoryPath = resolveDirectory(relativeDirectory);

            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path filePath = directoryPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return buildPublicPath(relativeDirectory, uniqueFilename);

        } catch (IOException e) {
            log.error("Failed to save uploaded image", e);
            throw new ImageException(ImageErrorCode.FILE_SAVE_FAILED);
        }
    }

    private String saveFileFromBytes(byte[] imageData, String filename) {
        try {
            LocalDate now = LocalDate.now();
            String datePath = String.format("%d/%02d/%02d",
                now.getYear(), now.getMonth().getValue(), now.getDayOfMonth());

            String relativeDirectory = "images/ai/" + datePath;
            Path directoryPath = resolveDirectory(relativeDirectory);

            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            Path filePath = directoryPath.resolve(filename);
            Files.write(filePath, imageData);

            return buildPublicPath(relativeDirectory, filename);

        } catch (IOException e) {
            log.error("Failed to save AI generated image", e);
            throw new ImageException(ImageErrorCode.FILE_SAVE_FAILED);
        }
    }

    private Path resolveDirectory(String relativeDirectory) {
        Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(relativeDirectory).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new ImageException(ImageErrorCode.FILE_SAVE_FAILED);
        }
        return targetPath;
    }

    private String buildPublicPath(String relativeDirectory, String filename) {
        return "/" + relativeDirectory + "/" + filename;
    }
}
