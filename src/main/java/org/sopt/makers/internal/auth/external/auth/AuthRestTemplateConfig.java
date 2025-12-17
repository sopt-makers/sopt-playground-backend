package org.sopt.makers.internal.auth.external.auth;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class AuthRestTemplateConfig {

    public static final String HEADER_API_KEY = "X-Api-Key";
    public static final String HEADER_SERVICE_NAME = "X-Service-Name";
    private static final int TIMEOUT_SECONDS = 5;

    @Bean
    public RestTemplate authRestTemplate(RestTemplateBuilder builder, AuthClientProperty property) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS));
        factory.setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS));

        return builder
                .rootUri(property.url())
                .requestFactory(() -> factory)
                .interceptors((request, body, execution) -> {
                    HttpHeaders headers = request.getHeaders();
                    headers.add(HEADER_API_KEY, property.apiKey());
                    headers.add(HEADER_SERVICE_NAME, property.serviceName());
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                    return execution.execute(request, body);
                })
                .build();
    }
}