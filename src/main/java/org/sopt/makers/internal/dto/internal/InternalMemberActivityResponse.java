package org.sopt.makers.internal.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record InternalMemberActivityResponse(
        @Schema(required = true)
        Long id,
        @Schema(required = true)
        String name,
        String profileImage,
        String phone,
        @Schema(required = true)
        List<InternalMemberActivityResponse.MemberSoptActivityResponse> activities
) {
    public record MemberSoptActivityResponse(
            Long id,
            Integer generation,
            String part,
            String team
    ) {}
}