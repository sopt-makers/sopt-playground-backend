package org.sopt.makers.internal.external.pushNotification;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.pushNotification.dto.PushNotificationRequest;
import org.sopt.makers.internal.external.pushNotification.dto.PushNotificationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    @Value("${push-notification.action}")
    private String action;

    @Value("${push-notification.x-api-key}")
    private String pushNotificationApiKey;

    @Value("${push-notification.service}")
    private String service;

    private final PushServerClient pushServerClient;

    public void sendPushNotification(PushNotificationRequest request) {
        PushNotificationResponse response = pushServerClient.sendPushNotification(
                pushNotificationApiKey,
                action,
                UUID.randomUUID().toString(),
                service,
                request
        );

        // TODO: 푸시알림 에러에 따른 플로우 처리
    }

    public void sendAllPushNotification(PushNotificationRequest request) {
        PushNotificationResponse response = pushServerClient.sendPushNotification(
                pushNotificationApiKey,
                "sendAll",
                UUID.randomUUID().toString(),
                service,
                request
        );

        // TODO: 푸시알림 에러에 따른 플로우 처리
    }
}
