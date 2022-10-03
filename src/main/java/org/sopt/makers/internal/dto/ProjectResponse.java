package org.sopt.makers.internal.dto;

import java.time.LocalDate;
import java.util.List;

public record ProjectResponse(
        Long id,
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
        List<ProjectMemberResponse> members,
        List<ProjectLinkResponse> links
) {
    public record ProjectMemberResponse(
            Long memberId,
            String memberRole,
            String memberDesc,
            Boolean isTeamMember
    ){}

    public record ProjectLinkResponse(
            Long linkId,
            String linkTitle,
            String linkUrl
    ){}
}
