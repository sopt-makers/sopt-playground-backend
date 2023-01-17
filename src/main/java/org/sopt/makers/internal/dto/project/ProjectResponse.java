package org.sopt.makers.internal.dto.project;

import java.util.List;

public record ProjectResponse(
        Long id,
        String name,
        Integer generation,
        String category,
        String[] serviceType,
        String summary,
        String detail,
        String logoImage,
        String thumbnailImage,
        List<ProjectResponse.ProjectLinkResponse> links
) {
    public record ProjectLinkResponse(
            Long linkId,
            String linkTitle,
            String linkUrl
    ){}
}
