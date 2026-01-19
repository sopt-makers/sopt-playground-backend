package org.sopt.makers.internal.common.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.pushNotification.message.PushNotificationMessageBuilder;

@Getter
@RequiredArgsConstructor
public class PushNotificationEvent implements NotificationEvent {

    private final PushNotificationMessageBuilder messageBuilder;

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }

    public static PushNotificationEvent of(PushNotificationMessageBuilder messageBuilder) {
        return new PushNotificationEvent(messageBuilder);
    }
}
