package org.sopt.makers.internal.dto.pushNotification;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PushNotificationResponse (

        @JsonProperty("status")
        Integer status,

        @JsonProperty("success")
        Boolean success,

        @JsonProperty("message")
        String message
) {}
