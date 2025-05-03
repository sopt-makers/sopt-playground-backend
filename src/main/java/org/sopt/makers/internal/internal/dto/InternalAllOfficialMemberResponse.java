package org.sopt.makers.internal.internal.dto;

import java.util.List;

public record InternalAllOfficialMemberResponse(
        List<InternalOfficialMemberResponse> members,
        int numberOfMembersAtGeneration
) {
}
