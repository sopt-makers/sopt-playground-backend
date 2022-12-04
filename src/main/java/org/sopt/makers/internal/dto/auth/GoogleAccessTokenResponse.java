package org.sopt.makers.internal.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken
) {}
