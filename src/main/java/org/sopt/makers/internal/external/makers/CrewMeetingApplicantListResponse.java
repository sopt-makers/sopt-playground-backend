package org.sopt.makers.internal.external.makers;

import java.util.List;

public record CrewMeetingApplicantListResponse(
        List<CrewMeetingApplicant> apply,
        PaginationMeta meta
) {
    public record CrewMeetingApplicant(
            Long id,
            Integer applyNumber,
            String content,
            String appliedDate,
            String status,
            ApplicantUser user
    ) {}

    public record ApplicantUser(
            Long id,
            String name,
            Long orgId,       // playground userId
            RecentActivity recentActivity,
            String profileImage,
            String phone
    ) {}

    public record RecentActivity(
            String part,
            Integer generation
    ) {}
}
