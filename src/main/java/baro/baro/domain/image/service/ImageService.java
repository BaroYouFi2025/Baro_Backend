package baro.baro.domain.image.service;

public interface ImageService {

    // 이미지 파일 업로드
    // @param file 업로드할 파일
    // @return 업로드된 파일의 URL
    String uploadImage(org.springframework.web.multipart.MultipartFile file);

    // byte[] 데이터로 이미지 저장 (AI 생성 이미지용)
    //
    // @param imageData 이미지 바이너리 데이터
    // @param filename 저장할 파일명 (확장자 포함)
    // @param mimeType MIME 타입 (예: "image/jpeg", "image/png")
    // @return 저장된 이미지의 접근 가능한 URL
    String saveImageFromBytes(byte[] imageData, String filename, String mimeType);
}
