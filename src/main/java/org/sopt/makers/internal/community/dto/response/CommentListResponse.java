package org.sopt.makers.internal.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentListResponse (
        @Schema(required = true)
        String content,

        @Schema(required = true)
        Boolean isBlindWriter,

        @Schema(required = true)
        Integer generation,

        @Schema(required = true)
        String part,

        @Schema(required = true)
        LocalDateTime createdAt,

        @Schema(required = false)
        Long parentCommentId
) {}
