package org.sopt.makers.internal.internal.dto;

import lombok.Builder;

@Builder
public record InternalPopularPostResponse(
        Long id,
        Long userId,
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
