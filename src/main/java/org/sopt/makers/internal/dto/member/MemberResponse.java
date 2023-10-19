package org.sopt.makers.internal.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberResponse(
        @Schema(required = true)
        Long id,

        @Schema(required = true)
        String name,
        @Schema(required = true)
        Integer generation,
        String profileImage,

        @Schema(required = true)
        Boolean hasProfile,

        @Schema(required = true)
        Boolean editActivitiesAble
) {
}
