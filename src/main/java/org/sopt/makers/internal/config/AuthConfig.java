package org.sopt.makers.internal.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AuthConfig {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${oauth.fb.redirect.auth}")
    private String redirectUriAuth;

    @Value("${oauth.fb.redirect.register}")
    private String redirectUriRegister;

    @Value("${oauth.fb.client.appId}")
    private String clientAppId;

    @Value("${oauth.fb.client.secret}")
    private String clientSecret;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${oauth.registerPage}")
    private String registerPage;

}
