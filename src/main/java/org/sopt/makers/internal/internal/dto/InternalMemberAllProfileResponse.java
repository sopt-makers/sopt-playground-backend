package org.sopt.makers.internal.internal.dto;

import java.util.List;

public record InternalMemberAllProfileResponse(
        List<InternalMemberProfileResponse> members,
        Boolean hasNext
) {
}
