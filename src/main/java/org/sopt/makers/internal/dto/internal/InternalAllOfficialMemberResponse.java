package org.sopt.makers.internal.dto.internal;

import java.util.List;

public record InternalAllOfficialMemberResponse(
        List<InternalOfficialMemberResponse> members,
        Integer numberOfMembers
) {
}
