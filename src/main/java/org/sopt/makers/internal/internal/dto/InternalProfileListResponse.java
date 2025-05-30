package org.sopt.makers.internal.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.sopt.makers.internal.member.dto.response.MemberProfileSpecificResponse;

import java.util.List;

@Builder
public record InternalProfileListResponse(

        @Schema(required = true)
        Long memberId,

        @Schema(required = true)
        String profileImage,

        @Schema(required = true)
        String name,

        @Schema(required = true)
        List<MemberProfileSpecificResponse.MemberCardinalInfoResponse> activities
) {
}
