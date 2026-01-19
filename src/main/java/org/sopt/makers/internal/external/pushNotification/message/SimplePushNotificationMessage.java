package org.sopt.makers.internal.external.pushNotification.message;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimplePushNotificationMessage implements PushNotificationMessageBuilder {

    private final String title;
    private final String content;
    private final Long[] recipientIds;
    private final String webLink;

    @Override
    public String buildTitle() {
        return title;
    }

    @Override
    public String buildContent() {
        return content;
    }

    @Override
    public Long[] getRecipientIds() {
        return recipientIds;
    }

    @Override
    public String getWebLink() {
        return webLink;
    }

    public static SimplePushNotificationMessage of(String title, String content, Long[] recipientIds, String webLink) {
        return new SimplePushNotificationMessage(title, content, recipientIds, webLink);
    }
}
