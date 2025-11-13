package baro.baro.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${police.api.url}")
    private String policeApiUrl;

    /**
     * 경찰청 실종자 API 전용 WebClient
     * - 타임아웃: 30초
     * - 연결 타임아웃: 10초
     * - 읽기/쓰기 타임아웃: 30초
     * - 비표준 Content-Type 'application/x-json' 지원
     */
    @Bean("policeApiWebClient")
    public WebClient policeApiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 연결 타임아웃 10초
                .responseTimeout(Duration.ofSeconds(30)) // 응답 타임아웃 30초
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));

        // 경찰청 API의 비표준 Content-Type 'application/x-json' 지원
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    // Increase buffer size to handle larger payloads from the police API
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder() {
                                @Override
                                public boolean canDecode(
                                        org.springframework.core.ResolvableType elementType,
                                        org.springframework.util.MimeType mimeType) {
                                    // application/x-json도 JSON으로 처리
                                    if (mimeType != null && "application".equals(mimeType.getType())) {
                                        String subtype = mimeType.getSubtype();
                                        if ("json".equals(subtype) || "x-json".equals(subtype)) {
                                            return true;
                                        }
                                    }
                                    return super.canDecode(elementType, mimeType);
                                }
                            }
                    );
                })
                .build();

        return WebClient.builder()
                .baseUrl(policeApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
