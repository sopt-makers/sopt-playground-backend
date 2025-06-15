package org.sopt.makers.internal.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccessTokenResponse(@Schema(required = true) String accessToken) {
}
