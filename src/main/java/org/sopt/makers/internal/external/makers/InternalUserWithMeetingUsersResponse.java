package org.sopt.makers.internal.external.makers;

import java.util.List;

public record InternalUserWithMeetingUsersResponse(
    List<UserOrgId> currentGenerationUserIds,
    List<UserOrgId> pastGenerationUserIds
) {
    public record UserOrgId(Integer orgUserId) {}
}
