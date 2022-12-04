package org.sopt.makers.internal.domain;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.sopt.makers.internal.dto.auth.GoogleUserInfoResponse;
import org.sopt.makers.internal.external.GoogleAuthClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class GoogleTokenManager {

    private final AuthConfig authConfig;
    private static final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static final HttpTransport transport = new NetHttpTransport();

    private final GoogleAuthClient authClient;
    private final GoogleUserInfoResponse failResponse = new GoogleUserInfoResponse(false, null);
    private final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(List.of(authConfig.getGoogleClientId()))
            .build();

    public String getAccessTokenByCode(String code, String redirectType) {
        val redirectUri = Objects.equals(redirectType, "auth") ? authConfig.getGoogleRedirectUriAuth() : authConfig.getGoogleRedirectUriRegister();
        val grantType = "authorization_code";
        val accessTokenResponse = authClient.getAccessToken(
                authConfig.getFbClientAppId(), authConfig.getFbClientSecret(), code, grantType, redirectUri
        );
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
