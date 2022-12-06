package org.sopt.makers.internal.domain;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.dto.auth.GoogleUserInfoResponse;
import org.sopt.makers.internal.external.GoogleAuthClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class GoogleTokenManager {

    private final AuthConfig authConfig;
    private final GoogleAuthClient authClient;
    private final GoogleIdTokenVerifier verifier;
    private final GoogleUserInfoResponse failResponse = new GoogleUserInfoResponse(false, null);

    public String getAccessTokenByCode(String code, String redirectType) {
        val redirectUri = Objects.equals(redirectType, "auth") ? authConfig.getGoogleRedirectUriAuth() : authConfig.getGoogleRedirectUriRegister();
        val grantType = "authorization_code";
        val body = Map.of(
                "client_id", authConfig.getGoogleClientId(),
                "client_secret", authConfig.getGoogleClientSecret(),
                "code", code,
                "grant_type", grantType,
                "redirect_uri", redirectUri
        );
        val accessTokenResponse = authClient.getAccessToken(body);
        return accessTokenResponse.accessToken();
    }

    public GoogleUserInfoResponse getUserInfo (String accessToken) {
        try {
            val idToken = verifier.verify(accessToken);
            if (idToken != null) {
                val payload = idToken.getPayload();
                val emailVerified = payload.getEmailVerified();
                val userId = payload.getSubject();
                return new GoogleUserInfoResponse(emailVerified, userId);
            }
            return failResponse;
        } catch (GeneralSecurityException | IOException e) {
            log.info(e.getMessage());
            return failResponse;
        }

    }

}
