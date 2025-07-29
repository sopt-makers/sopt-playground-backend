package org.sopt.makers.internal.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
public record InternalMemberProfileListResponse(
        @Schema(required = true)
        Long memberId,

        @Schema(required = true)
        String name,

        @Schema(required = true)
        String profileImage,

        @Schema(required = true)
        String introduction,

        @Schema(required = true, example = "36,서버")
        List<CardinalInfoResponse> activities
) {
}