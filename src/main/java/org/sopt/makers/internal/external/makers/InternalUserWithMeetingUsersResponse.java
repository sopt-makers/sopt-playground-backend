package org.sopt.makers.internal.external.makers;

import java.util.Collections;
import java.util.List;

public record InternalUserWithMeetingUsersResponse(
    List<UserOrgId> currentGenerationUserIds,
    List<UserOrgId> pastGenerationUserIds
) {
    public InternalUserWithMeetingUsersResponse {
        currentGenerationUserIds = currentGenerationUserIds != null ? currentGenerationUserIds : Collections.emptyList();
        pastGenerationUserIds = pastGenerationUserIds != null ? pastGenerationUserIds : Collections.emptyList();
    }

    public record UserOrgId(Integer orgUserId) {}
}
