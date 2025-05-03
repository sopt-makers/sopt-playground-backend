package org.sopt.makers.internal.external.pushNotification;

import org.sopt.makers.internal.external.pushNotification.dto.PushNotificationRequest;
import org.sopt.makers.internal.external.pushNotification.dto.PushNotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "pushServer", url = "${push-notification.server-url}")
public interface PushServerClient {

    @PostMapping("")
    PushNotificationResponse sendPushNotification(
            @RequestHeader("x-api-key") String pushNotificationApiKey,
            @RequestHeader("action") String action,
            @RequestHeader("transactionId") String transactionId,
            @RequestHeader("service") String service,
            @RequestBody PushNotificationRequest request
    );
}
