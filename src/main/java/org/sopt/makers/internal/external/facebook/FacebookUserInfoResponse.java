package org.sopt.makers.internal.external.facebook;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookUserInfoResponse (
        @JsonProperty("id")
        String userId,

        @JsonProperty("name")
        String userName
) {}
