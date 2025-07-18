package org.sopt.makers.internal.external.pushNotification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PushNotificationResponse (

        @JsonProperty("status")
        Integer status,

        @JsonProperty("success")
        Boolean success,

        @JsonProperty("message")
        String message
) {}
