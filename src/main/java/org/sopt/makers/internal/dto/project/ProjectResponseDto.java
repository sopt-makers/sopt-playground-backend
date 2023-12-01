package org.sopt.makers.internal.dto.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ProjectResponseDto(
        @Schema(required = true)
        Long id,

        @Schema(required = true)
        String name,

        @Schema(required = true)
        Long writerId,

        Integer generation,

        @Schema(required = true)
        String category,

        @Schema(required = true)
        LocalDate startAt,

        LocalDate endAt,

        String[] serviceType,

        @Schema(required = true)
        Boolean isAvailable,

        @Schema(required = true)
        Boolean isFounding,

        String summary,

        String detail,

        String logoImage,

        String thumbnailImage,

        String[] images,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) { }
