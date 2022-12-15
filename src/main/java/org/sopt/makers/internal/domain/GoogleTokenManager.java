package org.sopt.makers.internal.domain;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.dto.auth.GoogleAccessTokenResponse;
import org.sopt.makers.internal.dto.auth.GoogleUserInfoResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class GoogleTokenManager {

    private final AuthConfig authConfig;
    private final RestTemplate authClient = new RestTemplate();

    public GoogleAccessTokenResponse getAccessTokenByCode(String code, String redirectType) {
        val redirectUri = Objects.equals(redirectType, "auth") ? authConfig.getGoogleRedirectUriAuth() : authConfig.getGoogleRedirectUriRegister();
        val grantType = "authorization_code";
        val params = new LinkedMultiValueMap<>();
        params.add("client_id", authConfig.getGoogleClientId());
        params.add("client_secret", authConfig.getGoogleClientSecret());
        params.add("code", code);
        params.add("grant_type", grantType);
        params.add("redirect_uri", redirectUri);
        val host = "https://oauth2.googleapis.com/token";
        val accessTokenResponse = authClient.postForObject(host, params, GoogleAccessTokenResponse.class);
        return accessTokenResponse;
    }

    public String getUserInfo (String accessToken) {
        val host = "https://oauth2.googleapis.com/tokeninfo?id_token=";
        val url = host + accessToken.trim();
        val idToken = authClient.getForObject(url, GoogleUserInfoResponse.class);
        if (idToken == null) return null;
        return idToken.sub();
    }

}
