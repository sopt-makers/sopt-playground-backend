package org.sopt.makers.internal.domain;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.dto.FacebookUserInfoResponse;
import org.sopt.makers.internal.external.FacebookAuthClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class FacebookTokenManager {
    private final AuthConfig authConfig;
    private final FacebookAuthClient authClient;

    public String getAccessTokenByCode(String code, String redirectType) {
        val redirectUri = Objects.equals(redirectType, "auth") ? authConfig.getRedirectUriAuth() : authConfig.getRedirectUriRegister();
        val accessTokenResponse = authClient.getAccessToken(
                authConfig.getClientAppId(), redirectUri, authConfig.getClientSecret(), code
        );
        return accessTokenResponse.accessToken();
    }

    public FacebookUserInfoResponse getUserInfo(String accessToken) {
        return authClient.getUserInfo(accessToken);
    }

}
