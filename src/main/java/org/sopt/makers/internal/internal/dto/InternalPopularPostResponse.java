package org.sopt.makers.internal.internal.dto;

import lombok.Builder;

@Builder
public record InternalPopularPostResponse(
        Long id,
        String profileImage,
        String name,
        String generationAndPart,
        int rank,
        String category,
        String title,
        String content,
        String webLink
) {
}
