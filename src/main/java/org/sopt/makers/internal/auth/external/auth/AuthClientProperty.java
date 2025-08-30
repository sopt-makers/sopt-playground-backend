package org.sopt.makers.internal.auth.external.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.auth")
public record AuthClientProperty(
        String url,
        String apiKey,
        String serviceName,
        Endpoints endpoints
) {
    public record Endpoints(String jwk) {}
}