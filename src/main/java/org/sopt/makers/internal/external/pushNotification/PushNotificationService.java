package org.sopt.makers.internal.external.pushNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.external.ExternalConfig;
import org.sopt.makers.internal.external.pushNotification.dto.PushNotificationRequest;
import org.sopt.makers.internal.external.pushNotification.message.PushNotificationMessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final ExternalConfig externalConfig;
    private final PushServerClient pushServerClient;

    public void sendPushNotification(PushNotificationMessageBuilder messageBuilder) {
        try {
            String[] stringUserIds = Arrays.stream(messageBuilder.getRecipientIds())
                    .map(String::valueOf)
                    .toArray(String[]::new);

            PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                    .title(messageBuilder.buildTitle())
                    .content(messageBuilder.buildContent())
                    .userIds(stringUserIds)
                    .webLink(messageBuilder.getWebLink())
                    .category("NEWS")
                    .build();

            pushServerClient.sendPushNotification(
                    externalConfig.getPushNotificationApiKey(),
                    externalConfig.getAction(),
                    UUID.randomUUID().toString(),
                    externalConfig.getService(),
                    pushNotificationRequest
            );

            log.info("푸시 알림 전송 성공: {}", messageBuilder.getClass().getSimpleName());
        } catch (Exception exception) {
            log.error("푸시 알림 전송 실패: {} - {}", messageBuilder.getClass().getSimpleName(), exception.getMessage(), exception);
        }
    }

    /**
     * 레거시 메서드
     * @deprecated {@link #sendPushNotification(PushNotificationMessageBuilder)} 사용 권장
     */
    @Deprecated
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
                    externalConfig.getPushNotificationApiKey(),
                    externalConfig.getAction(),
                    UUID.randomUUID().toString(),
                    externalConfig.getService(),
                    pushNotificationRequest
            );
        } catch (Exception error) {
            log.error("Push 알림 실패: {}", error.getMessage());
        }
    }

    public void sendAllPushNotification(PushNotificationRequest request) {
        pushServerClient.sendPushNotification(
                externalConfig.getPushNotificationApiKey(),
                "sendAll",
                UUID.randomUUID().toString(),
                externalConfig.getService(),
                request
        );
    }
}
