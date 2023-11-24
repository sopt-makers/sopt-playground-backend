package org.sopt.makers.internal.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CommentResponse(
        @Schema(required = true)
        Long id,
        MemberVo member,
        Boolean isMine,
        Long postId,
        Long parentCommentId,
        String content,
        Boolean isBlindWriter,
        Boolean isReported,
        LocalDateTime createdAt
){}