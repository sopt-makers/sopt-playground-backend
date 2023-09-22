package org.sopt.makers.internal.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;

public record InternalMemberResponse(
        @Schema(required = true)
        Long id,

        @Schema(required = true)
        String name,
        @Schema(required = true)
        Integer latestGeneration,
        String profileImage,

        @Schema(required = true)
        Boolean hasProfile
) {}