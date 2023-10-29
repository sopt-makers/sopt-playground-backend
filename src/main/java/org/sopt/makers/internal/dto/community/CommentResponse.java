package org.sopt.makers.internal.dto.community;

import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.MemberSoptActivity;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        Long userId,
        String userName,
        String profileImage,
        List<MemberSoptActivity> activities,
        List<MemberCareer> careers,
        String content,
        Boolean isBlindWriter,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){}