package org.sopt.makers.internal.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.dto.member.MemberProfileResponse;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        @Schema(required = true)
        Long id,
        CommunityMemberResponse member,
        Long writerId,
        String title,
        String content,
        Integer hits,
        String[] images,
        Boolean isQuestion,
        Boolean isBlindWriter,
        LocalDateTime createdAt,
        List<CommentResponse> comments
) {}
