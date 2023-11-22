package org.sopt.makers.internal.external;

import org.sopt.makers.internal.dto.pushNotification.PushNotificationRequest;
import org.sopt.makers.internal.dto.pushNotification.PushNotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(value = "pushServer", url = "${push-notification.server-url}")
public interface PushServerClient {

    @PostMapping("")
    PushNotificationResponse sendPushNotification(
            @RequestHeader("x-api-key") String pushNotificationApiKey,
            @RequestHeader("action") String action,
            @RequestHeader("transactionId") UUID transactionId,
            @RequestHeader("service") String service,
            @RequestBody PushNotificationRequest request
    );
}
