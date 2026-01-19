package org.sopt.makers.internal.common.event;

/**
 * 알림 이벤트의 기본 인터페이스
 * 새로운 알림 유형 추가 시 이 인터페이스를 구현합니다.
 */
public interface NotificationEvent {

    /**
     * 알림 유형을 반환합니다.
     */
    NotificationType getType();

    /**
     * 알림 유형 정의
     */
    enum NotificationType {
        PUSH,
        SMS,
        EMAIL
    }
}
