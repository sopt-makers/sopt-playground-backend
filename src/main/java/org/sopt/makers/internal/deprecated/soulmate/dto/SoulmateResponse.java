package org.sopt.makers.internal.deprecated.soulmate.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SoulmateResponse (
        @Schema(required = true) boolean success,
        String code,
        String message
){}
