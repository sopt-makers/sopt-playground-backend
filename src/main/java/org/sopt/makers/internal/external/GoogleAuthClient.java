package org.sopt.makers.internal.external;

import org.sopt.makers.internal.dto.auth.GoogleAccessTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "googleAuth", url = "https://oauth2.googleapis.com")
public interface GoogleAuthClient {
    @PostMapping("/token")
    GoogleAccessTokenResponse getAccessToken(
            @RequestParam("client_id") String clientAppId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code,
            @RequestParam("grant_type") String grantType,
            @RequestParam("redirect_uri") String redirectUri
    );
}
