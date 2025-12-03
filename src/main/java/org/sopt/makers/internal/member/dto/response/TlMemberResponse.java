package org.sopt.makers.internal.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record TlMemberResponse(
        @Schema(required = true)
        Long id,
        @Schema(required = true)
        String name,
        String university,
        String profileImage,
        @Schema(required = true)
        List<MemberProfileResponse.MemberSoptActivityResponse> activities,
        String introduction
) {
}
