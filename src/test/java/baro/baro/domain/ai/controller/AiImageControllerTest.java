package baro.baro.domain.ai.controller;

import baro.baro.config.JwtAuthenticationFilter;
import baro.baro.domain.ai.dto.res.ApplyAiImageResponse;
import baro.baro.domain.ai.dto.res.GenerateAiImageResponse;
import baro.baro.domain.ai.entity.AssetType;
import baro.baro.domain.ai.service.AiImageService;
import baro.baro.domain.common.monitoring.MetricsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AiImageController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AiImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiImageService aiImageService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private MetricsService metricsService;

    @Test
    @DisplayName("AI 이미지 생성 성공 시 200과 이미지 URL 목록을 반환한다")
    void generateAiImage_success_returns200() throws Exception {
        // Given
        GenerateAiImageResponse response = GenerateAiImageResponse.create(
                AssetType.AGE_PROGRESSION,
                List.of(
                        "http://localhost:8080/images/ai/image1.png",
                        "http://localhost:8080/images/ai/image2.png",
                        "http://localhost:8080/images/ai/image3.png"
                )
        );
        when(aiImageService.generateImage(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/ai/images/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "missingPersonId", 1L,
                                "assetType", "AGE_PROGRESSION"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetType").value("AGE_PROGRESSION"))
                .andExpect(jsonPath("$.imageUrls").isArray())
                .andExpect(jsonPath("$.imageUrls.length()").value(3))
                .andExpect(jsonPath("$.imageUrls[0]").value("http://localhost:8080/images/ai/image1.png"))
                .andExpect(jsonPath("$.imageUrls[1]").value("http://localhost:8080/images/ai/image2.png"))
                .andExpect(jsonPath("$.imageUrls[2]").value("http://localhost:8080/images/ai/image3.png"));

        verify(aiImageService).generateImage(any());
    }

    @Test
    @DisplayName("AI 이미지 적용 성공 시 200과 응답을 반환한다")
    void applyAiImage_success_returns200() throws Exception {
        // Given
        ApplyAiImageResponse response = ApplyAiImageResponse.create(
                1L,
                AssetType.AGE_PROGRESSION,
                "http://localhost:8080/images/ai/selected-image.png"
        );
        when(aiImageService.applySelectedImage(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/ai/images/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "missingPersonId", 1L,
                                "assetType", "AGE_PROGRESSION",
                                "selectedImageUrl", "http://localhost:8080/images/ai/selected-image.png"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingPersonId").value(1L))
                .andExpect(jsonPath("$.assetType").value("AGE_PROGRESSION"))
                .andExpect(jsonPath("$.appliedUrl").value("http://localhost:8080/images/ai/selected-image.png"));

        verify(aiImageService).applySelectedImage(any());
    }
}
