package baro.baro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.dir}")
    private String uploadDir;
    
    /**
     * 업로드된 이미지를 정적 리소스로 제공
     * /images/** 요청을 로컬 uploads/images/ 디렉토리와 매핑
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations(absolutePath + "/images/");
    }
}
