package baro.baro.domain.image.entity;


import baro.baro.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "images", schema = "youfi")
@Data
public class Image {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 파일 저장 경로 (로컬 서버 경로)
    // 예: /uploads/images/2025/01/26/abc123.jpg
    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;
    
    // 원본 파일명
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    // 파일 크기 (bytes)
    @Column(name = "file_size")
    private Long fileSize;
    
    // MIME 타입 (image/jpeg, image/png 등)
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    // 이미지 소유자 (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 업로드 일시
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private ZonedDateTime uploadedAt;
    
    // 정적 팩토리 메서드
    public static Image create(String filePath, String originalFilename, Long fileSize, String mimeType, User user) {
        Image image = new Image();
        image.filePath = filePath;
        image.originalFilename = originalFilename;
        image.fileSize = fileSize;
        image.mimeType = mimeType;
        image.user = user;
        return image;
    }
}
