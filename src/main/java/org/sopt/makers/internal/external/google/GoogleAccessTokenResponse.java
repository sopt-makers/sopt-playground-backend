package org.sopt.makers.internal.external.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleAccessTokenResponse(
        @JsonProperty("id_token")
        String idToken
) {}
