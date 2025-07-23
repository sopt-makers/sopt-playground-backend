package org.sopt.makers.internal.project.dto.response.detailProject;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailResponse(
        @Schema(required = true)
        Long id,
        @Schema(required = true)
        String name,
        @Schema(required = true)
        Long writerId,
        @Schema(required = true)
        Integer generation,
        @Schema(required = true)
        String category,
        @Schema(required = true)
        LocalDate startAt,
        LocalDate endAt,
        @Schema(required = true)
        String[] serviceType,
        Boolean isAvailable,
        Boolean isFounding,
        @Schema(required = true)
        String summary,
        @Schema(required = true)
        String detail,
        @Schema(required = true)
        String logoImage,
        @Schema(required = true)
        String thumbnailImage,
        String[] images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProjectDetailMemberResponse> members,
        List<ProjectLinkResponse> links
) { }
