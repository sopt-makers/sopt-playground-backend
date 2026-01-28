package org.sopt.makers.internal.external.platform;

public record SoptActivity(
        int activityId,
        int generation,
        String part,
        String team,
        String role,
        boolean isSopt
) {}