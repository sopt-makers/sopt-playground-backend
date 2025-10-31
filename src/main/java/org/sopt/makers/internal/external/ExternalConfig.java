package org.sopt.makers.internal.external;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ExternalConfig {

    // Slack
    @Value("${push-notification.action}")
    private String action;

    @Value("${push-notification.x-api-key}")
    private String pushNotificationApiKey;

    @Value("${push-notification.service}")
    private String service;
}
