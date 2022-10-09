package org.sopt.makers.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookUserInfoResponse (
        @JsonProperty("id")
        Long userId,

        @JsonProperty("name")
        Long userName
) {}
