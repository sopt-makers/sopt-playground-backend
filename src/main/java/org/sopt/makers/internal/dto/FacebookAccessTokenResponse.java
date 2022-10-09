package org.sopt.makers.internal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken
) {}
