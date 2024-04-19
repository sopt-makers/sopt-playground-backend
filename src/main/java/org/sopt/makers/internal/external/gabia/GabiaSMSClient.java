package org.sopt.makers.internal.external.gabia;

import org.sopt.makers.internal.dto.auth.GabiaAuthRequest;
import org.sopt.makers.internal.dto.auth.GabiaAuthResponse;
import org.sopt.makers.internal.dto.auth.GabiaSMSRequest;
import org.sopt.makers.internal.dto.auth.GabiaSMSResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "gabiaSMSClient", url = "https://sms.gabia.com")
public interface GabiaSMSClient {

    @PostMapping("/oauth/token")
    GabiaAuthResponse getGabiaAccessToken(
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("cache-control") String cacheControl,
            @RequestBody GabiaAuthRequest gabiaSMSRequest
    );

    @PostMapping("/api/send/sms")
    GabiaSMSResponse sendSMS(
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("Authorization") String accessToken,
            @RequestBody GabiaSMSRequest gabiaSMSRequest
    );
}
