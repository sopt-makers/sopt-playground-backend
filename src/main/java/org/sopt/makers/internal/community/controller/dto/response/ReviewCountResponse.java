package org.sopt.makers.internal.community.controller.dto.response;

import java.util.List;

public record ReviewCountResponse(
        int reviewCount,
        List<ReviewDto> reviews
) {
    public record ReviewDto(
            Long id,
            String title,
            String author,
            String authorProfileImageUrl,
            int generation,
            String description,
            String part,
            String subject,
            String thumbnailUrl,
            String platform,
            String url
    ) {}
}
