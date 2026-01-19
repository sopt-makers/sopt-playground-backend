package org.sopt.makers.internal.common.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.pushNotification.message.PushNotificationMessageBuilder;

/**
 * 푸시 알림 이벤트
 * PushNotificationMessageBuilder를 감싸서 이벤트로 발행합니다.
 */
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
