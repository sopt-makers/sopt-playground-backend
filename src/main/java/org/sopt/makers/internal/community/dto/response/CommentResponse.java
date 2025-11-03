package org.sopt.makers.internal.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.community.dto.AnonymousProfileVo;
import org.sopt.makers.internal.community.dto.MemberVo;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        @Schema(required = true)
        Long id,
        MemberVo member,
        Boolean isMine,
        Long postId,
        Long parentCommentId,
        String content,
        Boolean isBlindWriter,
        AnonymousProfileVo anonymousProfile,
        Boolean isReported,
        LocalDateTime createdAt,
        Boolean isDeleted,
        List<CommentResponse> replies
){}