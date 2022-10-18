package org.sopt.makers.internal.external;

import org.sopt.makers.internal.dto.FacebookAccessTokenResponse;
import org.sopt.makers.internal.dto.FacebookUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "fbAuth", url = "https://graph.facebook.com")
public interface FacebookAuthClient {
    @GetMapping("/v13.0/oauth/access_token")
    FacebookAccessTokenResponse getAccessToken(
            @RequestParam("client_id") String clientAppId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code
    );

    @GetMapping("/me")
    FacebookUserInfoResponse getUserInfo(
            @RequestParam("access_token") String accessToken
    );
}
