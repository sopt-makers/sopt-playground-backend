package org.sopt.makers.internal.dto.sopticle;

import io.swagger.v3.oas.annotations.media.Schema;

public record SopticleResponse (
        @Schema(required = true) boolean success,
        String code,
        String message
){}
