package org.sopt.makers.internal.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ProjectUpdateRequest(
        @Schema(required = true)
        String name,
        @Schema(required = true)
        Integer generation,
        @Schema(required = true)
        String category,
        @Schema(required = true)
        LocalDate startAt,
        LocalDate endAt,
        Boolean isAvailable,
        @Schema(required = true)
        String summary,
        @Schema(required = true)
        String detail,
        @Schema(required = true)
        String logoImage,
        @Schema(required = true)
        String thumbnailImage,
        String[] images,
        List<ProjectMemberUpdateRequest> members,
        List<ProjectLinkUpdateRequest> links
) {
    public record ProjectMemberUpdateRequest(
            Long memberId,
            String memberRole,
            String memberDescription,
            Boolean isTeamMember
    ){}

    public record ProjectLinkUpdateRequest(
            Long linkId,
            String linkTitle,
            String linkUrl
    ){}
}
