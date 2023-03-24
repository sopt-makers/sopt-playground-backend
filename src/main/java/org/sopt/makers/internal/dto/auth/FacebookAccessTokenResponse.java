package org.sopt.makers.internal.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record FacebookAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken
) {}
