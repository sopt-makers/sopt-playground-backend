package org.sopt.makers.internal.project.dto.response.allProject;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProjectResponse(
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
        List<ProjectMemberResponse> members
        // 0715 변경사항 - List<ProjectLinkResponse> links 삭제 (오류 롤백 주석)
) { }
