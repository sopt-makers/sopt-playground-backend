package org.sopt.makers.internal.member.dto.response;

import java.util.List;

public record MemberAllProfileResponse(
        List<MemberProfileResponse> members,
        Boolean hasNext,
        Integer totalMembersCount
) {}
