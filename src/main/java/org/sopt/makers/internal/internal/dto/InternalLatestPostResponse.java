package org.sopt.makers.internal.internal.dto;

import lombok.Builder;

@Builder
public record InternalLatestPostResponse(
        Long id,
        Long userId,
        String profileImage,
        String name,
        String generationAndPart,
        String category,
        String title,
        String content,
        String webLink,
        String createdAt
) {
}
