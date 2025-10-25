package baro.baro.domain.image.service;

public interface ImageService {
    
    /**
     * 이미지 파일 업로드
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL
     */
    String uploadImage(org.springframework.web.multipart.MultipartFile file);
}
