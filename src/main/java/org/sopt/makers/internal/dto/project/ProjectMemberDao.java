package org.sopt.makers.internal.dto.project;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectMemberDao (
        Long id,
        String name,
        Long writerId,
        Integer generation,
        String category,
        LocalDate startAt,
        LocalDate endAt,
        String[] serviceType,
        Boolean isAvailable,
        Boolean isFounding,
        String summary,
        String detail,
        String logoImage,
        String thumbnailImage,
        String[] images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Long memberId,
        String memberName,
        Integer memberGeneration,

        String memberRole,
        String memberDesc,
        Boolean isTeamMember
) {
    @QueryProjection
    public ProjectMemberDao {}
}
