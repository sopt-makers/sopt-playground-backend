package org.sopt.makers.internal.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType
) {}
