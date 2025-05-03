package org.sopt.makers.internal.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProjectResponse(
        @Schema(required = true)
        Long id,
        @Schema(required = true)
        String name,
        @Schema(required = true)
        Integer generation,
        @Schema(required = true)
        String category,
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
        List<ProjectMemberResponse> members,
        List<ProjectLinkResponse> links
) {
    public record ProjectMemberResponse(
            Long memberId,
            String memberName,
            String memberProfileImage
    ){}
    public record ProjectLinkResponse(
            Long linkId,
            String linkTitle,
            String linkUrl
    ){}
}
