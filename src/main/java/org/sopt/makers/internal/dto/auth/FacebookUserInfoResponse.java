package org.sopt.makers.internal.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookUserInfoResponse (
        @JsonProperty("id")
        String userId,

        @JsonProperty("name")
        String userName
) {}
