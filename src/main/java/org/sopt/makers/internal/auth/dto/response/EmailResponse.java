package org.sopt.makers.internal.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmailResponse(
        @Schema(required = true)
        boolean success,
        String code,
        String message
) {}
