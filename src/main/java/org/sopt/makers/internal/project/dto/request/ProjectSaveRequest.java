package org.sopt.makers.internal.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record ProjectSaveRequest(
        @Schema(required = true)
        String name,
        @Schema(required = true)
        Long writerId,
        @Schema(required = true)
        Integer generation,
        @Schema(required = true)
        String category,
        @Schema(required = true)
        LocalDate startAt,
        LocalDate endAt,
        @Schema(required = true)
        List<String> serviceType,
        Boolean isAvailable,
        Boolean isFounding,
        @Schema(required = true)
        String summary,
        @Schema(required = true)
        String detail,
        @Schema(required = true)
        String logoImage,
        @Schema(required = true)
        String thumbnailImage,
        List<String> images,
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
