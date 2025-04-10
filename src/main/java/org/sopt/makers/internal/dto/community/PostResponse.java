package org.sopt.makers.internal.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        @Schema(required = true)
        Long id,
        MemberVo member,
        Long writerId,
        Boolean isMine,
        Boolean isLiked,
        Integer likes,
        Long categoryId,
        String categoryName,
        String title,
        String content,
        Integer hits,
        Integer commentCount,
        String[] images,
        Boolean isQuestion,
        Boolean isBlindWriter,
        Boolean isSopticle,
        String sopticleUrl,
        AnonymousProfileVo anonymousProfile,
        LocalDateTime createdAt,
        List<CommentResponse> comments
) {}
