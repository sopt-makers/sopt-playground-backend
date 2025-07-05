package org.sopt.makers.internal.external.pushNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.external.pushNotification.dto.PushNotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
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

    public void sendPushNotification(String title, String content, Long[] userIds, String webLink) {
        try {
            String[] stringUserIds = Arrays.stream(userIds)
                    .map(String::valueOf)
                    .toArray(String[]::new);

            PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                    .title(title)
                    .content(content)
                    .userIds(stringUserIds)
                    .webLink(webLink)
                    .category("NEWS").build();

            pushServerClient.sendPushNotification(
                    pushNotificationApiKey,
                    action,
                    UUID.randomUUID().toString(),
                    service,
                    pushNotificationRequest
            );
        } catch (Exception error) {
            log.error("Push 알림 실패: {}", error.getMessage());
        }
    }

    public void sendAllPushNotification(PushNotificationRequest request) {
        pushServerClient.sendPushNotification(
                pushNotificationApiKey,
                "sendAll",
                UUID.randomUUID().toString(),
                service,
                request
        );
    }
}
