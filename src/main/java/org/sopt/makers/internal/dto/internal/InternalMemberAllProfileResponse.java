package org.sopt.makers.internal.dto.internal;

import java.util.List;

public record InternalMemberAllProfileResponse(
        List<InternalMemberProfileResponse> members,
        Boolean hasNext
) {
}
