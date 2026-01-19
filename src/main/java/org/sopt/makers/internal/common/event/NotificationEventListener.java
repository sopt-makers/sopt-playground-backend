package org.sopt.makers.internal.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 알림 이벤트 리스너
 * 트랜잭션 커밋 후 비동기로 알림을 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final PushNotificationService pushNotificationService;

    /**
     * 푸시 알림 이벤트 처리
     * - @TransactionalEventListener: 트랜잭션 커밋 후 실행 (데이터 일관성 보장)
     * - @Async: 별도 스레드에서 비동기 실행 (API 응답 시간에 영향 없음)
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePushNotificationEvent(PushNotificationEvent event) {
        log.info("푸시 알림 이벤트 수신: {}", event.getMessageBuilder().getClass().getSimpleName());
        pushNotificationService.sendPushNotification(event.getMessageBuilder());
    }
}
