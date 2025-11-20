package baro.baro.domain.ai.client;

import baro.baro.domain.ai.exception.AiErrorCode;
import baro.baro.domain.ai.exception.AiException;
import baro.baro.domain.ai.processing.ImageProcessingService;
import baro.baro.domain.ai.service.RateLimiter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiApiClientTest {

    @Mock
    private ImageProcessingService imageProcessingService;

    private GeminiApiClient geminiApiClient;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder().build();
        geminiApiClient = new GeminiApiClient(webClient, new RateLimiter(), imageProcessingService);

        ReflectionTestUtils.setField(geminiApiClient, "geminiApiKey", "dummy-key");
        ReflectionTestUtils.setField(geminiApiClient, "geminiImageUrl", mockWebServer.url("/v1beta").toString());
        ReflectionTestUtils.setField(geminiApiClient, "quotaCheckEnabled", false);
        ReflectionTestUtils.setField(geminiApiClient, "maxRetryAttempts", 1);
        ReflectionTestUtils.setField(geminiApiClient, "retryDelaySeconds", 1);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void generateImageReturnsDecodedBytesWhenApiRespondsWithInlineData() {
        String expectedBase64 = Base64.getEncoder().encodeToString("hello-image".getBytes(StandardCharsets.UTF_8));
        String body = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "inlineData": {
                          "mimeType": "image/png",
                          "data": "%s"
                        }
                      }
                    ]
                  }
                }
              ]
            }
            """.formatted(expectedBase64);

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        byte[] result = geminiApiClient.generateImage("input", "image/png", "prompt text");

        assertThat(result).isEqualTo(Base64.getDecoder().decode(expectedBase64));
    }

    @Test
    void generateImageReturnsPlaceholderWhenApiKeyMissing() {
        byte[] placeholder = "fallback".getBytes(StandardCharsets.UTF_8);
        lenient().when(imageProcessingService.generatePlaceholderImage()).thenReturn(placeholder);
        ReflectionTestUtils.setField(geminiApiClient, "geminiApiKey", "");

        byte[] result = geminiApiClient.generateImage("ignored", "image/png", "prompt");

        assertThat(result).isEqualTo(placeholder);
    }

    @Test
    void generateImageThrowsWhenResponseHasNoImage() {
        String emptyBody = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "policy warning"
                      }
                    ]
                  }
                }
              ]
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(emptyBody));

        assertThatThrownBy(() ->
                geminiApiClient.generateImage("input", "image/png", "prompt")
        ).isInstanceOf(AiException.class)
         .extracting(ex -> ((AiException) ex).getAiErrorCode())
         .isEqualTo(AiErrorCode.EMPTY_RESPONSE);
    }
}
