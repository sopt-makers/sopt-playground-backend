package org.sopt.makers.internal.dto.pushNotification;

import lombok.Builder;

@Builder
public record PushNotificationRequest(

        String[] userIds,
        String title,
        String content,
        String category,
        String webLink
) {}
