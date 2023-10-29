package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.MemberSoptActivity;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryPostMemberDao(
        Long id,
        Long categoryId,
        Long userId,
        String userName,
        String profileImage,
        List<MemberSoptActivity> activities,
        List<MemberCareer> careers,
        String title,
        String content,
        Integer hits,
        Boolean isQuestion,
        Boolean isBlindWriter,
        String[] images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    @QueryProjection
    public CategoryPostMemberDao {}
}
