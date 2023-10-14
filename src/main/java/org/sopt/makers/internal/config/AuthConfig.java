package org.sopt.makers.internal.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Configuration
public class AuthConfig {
    @Value("${spring.profiles.active}")
    private String activeProfile;

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

    @Value("${oauth.apple.magic-number}")
    private String magicNumber;

    @Value("${oauth.apple.register-token}")
    private String magicRegisterToken;

    @Value("${oauth.dev-register-qa-token}")
    private String devRegisterQaToken;

    @Value("${oauth.dev-register-magic-number}")
    private String devRegisterMagicNumber;

    @Value("${oauth.apple.key.url}")
    private String applePublicKeysUrl;

    @Value("${oauth.apple.aud}")
    private String appleAud;

    @Value("${oauth.apple.sub}")
    private String appleSub;

    @Value("${oauth.apple.team.id}")
    private String appleTeamId;

    @Value("${oauth.apple.key.id}")
    private String appleKeyId;

    @Value("${oauth.apple.key.path}")
    private String appleKeyPath;

    @Value("${oauth.apple.auth.token.url}")
    private String appleAuthTokenUrl;

    @Value("${oauth.apple.revoke.url}")
    private String appleRevokeUrl;

    @Value("${internal.app.secret}")
    private String appApiSecretKey;

    @Value("${internal.official.sopticle-key}")
    private String officialSopticleApiSecretKey;

    @Value("${dictionary.key}")
    private String dictionaryKey;
}
