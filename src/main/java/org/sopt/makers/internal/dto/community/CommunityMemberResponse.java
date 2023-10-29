package org.sopt.makers.internal.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.MemberSoptActivity;

import java.util.List;

public record CommunityMemberResponse(
        Long id,
        String name,
        String image,
        @Schema(required = true)
        List<MemberSoptActivity> activities,
        List<MemberCareer> careers
){}