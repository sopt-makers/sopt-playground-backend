package org.sopt.makers.internal.project.dto;

import org.springframework.aot.hint.annotation.Reflective;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Reflective
public record ProjectMemberVo(
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
        List<Integer> memberGenerations,
        String memberProfileImage,
        Boolean memberHasProfile,

        String memberRole,
        String memberDesc,
        Boolean isTeamMember
) {
}
