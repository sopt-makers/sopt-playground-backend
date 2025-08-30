package org.sopt.makers.internal.external.platform;

import java.util.List;

public record InternalUserDetails(
        Long userId,
        String name,
        String profileImage,
        String birthday,
        String phone,
        String email,
        int lastGeneration,
        List<SoptActivity> soptActivities
) {}
