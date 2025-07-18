package org.sopt.makers.internal.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.sopt.makers.internal.project.dto.response.detailProject.ProjectLinkResponse;

public record InternalProjectResponse(
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

        @Schema(required = true)
        Boolean isAvailable,

        @Schema(required = true)
        Boolean isFounding,

        List<ProjectLinkResponse> links
) { }