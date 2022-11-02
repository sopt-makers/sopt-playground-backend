package org.sopt.makers.internal.dto.project;

import java.time.LocalDate;
import java.util.List;

public record ProjectSaveRequest(
        String name,
        Long writerId,
        Integer generation,
        String category,
        LocalDate startAt,
        LocalDate endAt,
        String[] serviceType,
        Boolean isAvailable,
        Boolean isFounding,
        String summary,
        String detail,
        String logoImage,
        String thumbnailImage,
        String[] images,
        List<ProjectMemberSaveRequest> members,
        List<ProjectLinkSaveRequest> links
) {
    public record ProjectMemberSaveRequest(
            Long memberId,
            String memberRole,
            String memberDescription,
            Boolean isTeamMember
    ){}

    public record ProjectLinkSaveRequest(
            String linkTitle,
            String linkUrl
    ){}
}
