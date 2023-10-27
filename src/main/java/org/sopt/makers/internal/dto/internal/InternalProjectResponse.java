package org.sopt.makers.internal.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

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
) {
    public record ProjectLinkResponse(
            Long linkId,
            String linkTitle,
            String linkUrl
    ){}
}