package org.sopt.makers.internal.project.dto.response.detailProject;

import java.util.List;

public record ProjectDetailMemberResponse(
        Long memberId,
        String memberRole,
        String memberDescription,
        Boolean isTeamMember,
        String memberName,
        List<Integer> memberGenerations,
        String memberProfileImage,
        Boolean memberHasProfile
){}
