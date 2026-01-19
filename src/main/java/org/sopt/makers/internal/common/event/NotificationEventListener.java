package org.sopt.makers.internal.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final PushNotificationService pushNotificationService;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePushNotificationEvent(PushNotificationEvent event) {
        log.info("푸시 알림 이벤트 수신: {}", event.getMessageBuilder().getClass().getSimpleName());
        pushNotificationService.sendPushNotification(event.getMessageBuilder());
    }
}
