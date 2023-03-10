package org.sopt.makers.internal.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Configuration
public class AuthConfig {
    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${oauth.fb.redirect.auth}")
    private String fbRedirectUriAuth;

    @Value("${oauth.fb.redirect.register}")
    private String fbRedirectUriRegister;

    @Value("${oauth.fb.client.appId}")
    private String fbClientAppId;

    @Value("${oauth.fb.client.secret}")
    private String fbClientSecret;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${oauth.registerPage}")
    private String registerPage;

    @Value("${oauth.google.client.id}")
    private String googleClientId;

    @Value("${oauth.google.client.secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect.auth}")
    private String googleRedirectUriAuth;

    @Value("${oauth.google.redirect.register}")
    private String googleRedirectUriRegister;

    @Value("${coffeechat.profile}")
    private String profileUrl;

    @Value("${coffeechat.logo}")
    private String logoUrl;

    @Value("${coffeechat.default}")
    private String profileDefaultUrl;

    @Value("${jwt.code}")
    private String secretForCode;

    @Value("${naver-cloud-sms.accessKey}")
    private String smsAccessKey;

    @Value("${naver-cloud-sms.secretKey}")
    private String smsSecretKey;

    @Value("${naver-cloud-sms.serviceId}")
    private String smsServiceId;

    @Value("${naver-cloud-sms.senderPhone}")
    private String smsSenderPhone;
}
