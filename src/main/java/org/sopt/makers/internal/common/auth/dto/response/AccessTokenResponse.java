package org.sopt.makers.internal.common.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccessTokenResponse(@Schema(required = true) String accessToken) {
}
