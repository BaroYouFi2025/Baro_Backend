package baro.baro.domain.image.dto;

import lombok.Data;

@Data
public class ImageUploadResponse {
    
    private String url;
    
    public static ImageUploadResponse create(String url) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.url = url;
        return response;
    }
}
