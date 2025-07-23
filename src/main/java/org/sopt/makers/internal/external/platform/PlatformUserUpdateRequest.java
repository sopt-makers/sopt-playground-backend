package org.sopt.makers.internal.external.platform;

import java.util.List;

public record PlatformUserUpdateRequest(
        String name,
        String profileImage,
        String birthday,
        String phone,
        String email,
        List<SoptActivityRequest> soptActivities
) {
    public record SoptActivityRequest(
            int activityId,
            String team
    ) {}
}