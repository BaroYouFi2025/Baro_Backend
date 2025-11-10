package baro.baro.domain.image.service;

import baro.baro.domain.image.entity.Image;
import baro.baro.domain.image.repository.ImageRepository;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

import static baro.baro.domain.common.util.SecurityUtil.getCurrentUser;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository imageRepository;
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    @Value("${file.upload.base-url}")
    private String baseUrl;
    
    @Override
    public String uploadImage(MultipartFile file) {
        // 현재 로그인한 사용자 조회
        User user = getCurrentUser();
        
        // 파일 검증
        validateFile(file);
        
        // 파일 저장 경로 생성 (년/월/일 구조)
        String savedPath = saveFile(file);
        
        // DB에 이미지 정보 저장
        Image image = Image.create(
            savedPath,
            file.getOriginalFilename(),
            file.getSize(),
            file.getContentType(),
            user
        );
        imageRepository.save(image);
        
        // 접근 가능한 URL 반환
        return baseUrl + savedPath;
    }
    
    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        // 파일 크기 제한 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }
    
    @Override
    public String saveImageFromBytes(byte[] imageData, String filename, String mimeType) {
        // 파일 데이터 검증
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("이미지 데이터가 비어있습니다.");
        }

        // 파일 크기 제한 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (imageData.length > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 파일 저장
        String savedPath = saveFileFromBytes(imageData, filename);

        // 접근 가능한 URL 반환
        return baseUrl + savedPath;
    }

    /**
     * 파일을 로컬에 저장하고 저장 경로 반환
     */
    private String saveFile(MultipartFile file) {
        try {
            // 현재 날짜 기반 디렉토리 생성 (예: /uploads/images/2025/01/26/)
            LocalDate now = LocalDate.now();
            String datePath = String.format("%d/%02d/%02d",
                now.getYear(), now.getMonth().getValue(), now.getDayOfMonth());

            String relativePath = "/images/" + datePath;
            Path directoryPath = Paths.get(uploadDir + relativePath);

            // 디렉토리 생성
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // 고유한 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = directoryPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 상대 경로 반환
            return relativePath + "/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * byte[] 데이터를 로컬에 저장하고 저장 경로 반환 (AI 생성 이미지용)
     */
    private String saveFileFromBytes(byte[] imageData, String filename) {
        try {
            // 현재 날짜 기반 디렉토리 생성 + ai 폴더
            LocalDate now = LocalDate.now();
            String datePath = String.format("%d/%02d/%02d",
                now.getYear(), now.getMonth().getValue(), now.getDayOfMonth());

            String relativePath = "/images/ai/" + datePath;
            Path directoryPath = Paths.get(uploadDir + relativePath);

            // 디렉토리 생성
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // 파일 저장
            Path filePath = directoryPath.resolve(filename);
            Files.write(filePath, imageData);

            // 상대 경로 반환
            return relativePath + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }
}
