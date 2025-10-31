package org.sopt.makers.internal.external.pushNotification.message;

public interface PushNotificationMessageBuilder {
    String buildTitle();
    String buildContent();
    Long[] getRecipientIds();
    String getWebLink();
}
