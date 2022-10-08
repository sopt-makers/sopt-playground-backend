package org.sopt.makers.internal.dto;

import java.time.LocalDate;
import java.util.List;

public record ProjectUpdateRequest(
        String name,
        Integer generation,
        String category,
        LocalDate startAt,
        LocalDate endAt,
        Boolean isAvailable,
        String summary,
        String detail,
        String logoImage,
        String thumbnailImage,
        String[] images,
        List<ProjectMemberUpdateRequest> members,
        List<ProjectLinkUpdateRequest> links
) {
    public record ProjectMemberUpdateRequest(
            Long memberId,
            String memberRole,
            String memberDesc,
            Boolean isTeamMember
    ){}

    public record ProjectLinkUpdateRequest(
            Long linkId,
            String linkTitle,
            String linkUrl
    ){}
}
