package org.sopt.makers.internal.dto.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailResponse(
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProjectMemberResponse> members,
        List<ProjectLinkResponse> links
) {
    public record ProjectMemberResponse(
            Long memberId,
            String memberRole,
            String memberDescription,
            Boolean isTeamMember,
            String memberName,
            List<Integer> memberGenerations,
            String memberProfileImage,
            Boolean memberHasProfile
    ){}

    public record ProjectLinkResponse(
            Long linkId,
            String linkTitle,
            String linkUrl
    ){}
}
