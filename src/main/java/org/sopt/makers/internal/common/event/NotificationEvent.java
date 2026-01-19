package org.sopt.makers.internal.common.event;

public interface NotificationEvent {

    NotificationType getType();

    enum NotificationType {
        PUSH,
        SMS,
        EMAIL
    }
}
