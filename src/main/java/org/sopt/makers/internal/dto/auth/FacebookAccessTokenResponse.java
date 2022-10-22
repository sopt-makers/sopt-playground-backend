package org.sopt.makers.internal.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken
) {}
