package org.sopt.makers.internal.dto.project;

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
        @Schema(required = true)
        String summary,
        @Schema(required = true)
        String detail,
        @Schema(required = true)
        String logoImage,
        @Schema(required = true)
        String thumbnailImage,
        List<ProjectResponse.ProjectLinkResponse> links
) {
    public record ProjectLinkResponse(
            Long linkId,
            String linkTitle,
            String linkUrl
    ){}
}
