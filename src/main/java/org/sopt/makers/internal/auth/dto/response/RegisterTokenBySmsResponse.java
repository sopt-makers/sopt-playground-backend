package org.sopt.makers.internal.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterTokenBySmsResponse(
        @Schema(required = true) boolean success,
        String code,
        String message,
        String registerToken
) {
}
