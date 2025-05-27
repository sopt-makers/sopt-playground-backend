package org.sopt.makers.internal.community.dto.response;

import java.time.LocalDateTime;

public record SopticleScrapedResponse(
        String part,
        String thumbnailUrl,
        String title,
        String description,
        int generation,
        String author,
        String authorProfileImageUrl,
        long id,
        String url,
        LocalDateTime uploadedAt
) {
}
