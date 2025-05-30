package org.sopt.makers.internal.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InternalProjectDetailResponse(
        @Schema(required = true)
        Long id,
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
        String[] serviceType,
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

