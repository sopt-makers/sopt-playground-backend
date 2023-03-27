package org.sopt.makers.internal.dto.internal;

import org.sopt.makers.internal.dto.member.MemberProfileResponse;

import java.util.List;

public record InternalMemberAllProfileResponse(
        List<InternalMemberProfileResponse> members,
        Boolean hasNext
) {
}
