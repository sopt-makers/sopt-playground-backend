package org.sopt.makers.internal.dto.member;

import java.util.List;

public record MemberAllProfileResponse(
        List<MemberProfileResponse> members,
        Boolean hasNext
) {}
