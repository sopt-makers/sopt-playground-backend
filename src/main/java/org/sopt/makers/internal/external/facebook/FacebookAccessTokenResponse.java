package org.sopt.makers.internal.external.facebook;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FacebookAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken
) {}
