package org.sopt.makers.internal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record SmsCodeResponse(
        @Schema(required = true) boolean success,
        String code,
        String message
) {}
