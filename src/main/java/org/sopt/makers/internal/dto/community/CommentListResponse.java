package org.sopt.makers.internal.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CommentListResponse (
        @Schema(required = true)
        String content,

        @Schema(required = true)
        Boolean isBlindWriter,

        @Schema(required = false)
        Long parentCommentId
) {}
