package org.sopt.makers.internal.domain;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.dto.FacebookUserInfoResponse;
import org.sopt.makers.internal.external.FacebookAuthClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;

@RequiredArgsConstructor
public class FacebookTokenManager {

    @Value("${oauth.fb.redirect.auth}")
    private final String redirectUriAuth;

    @Value("${oauth.fb.redirect.register}")
    private final String redirectUriRegister;

    @Value("${oauth.fb.client.appId}")
    private final String clientAppId;

    @Value("${oauth.fb.client.secret}")
    private final String clientSecret;

    private final FacebookAuthClient authClient;

    public String getAccessTokenByCode(String code, String redirectType) {
        val redirectUri = Objects.equals(redirectType, "auth") ? redirectUriAuth : redirectUriRegister;
        val accessTokenResponse = authClient.getAccessToken(clientAppId, redirectUri, clientSecret, code);
        return accessTokenResponse.accessToken();
    }

    public FacebookUserInfoResponse getUserInfo(String accessToken) {
        return authClient.getUserInfo(accessToken);
    }

}
