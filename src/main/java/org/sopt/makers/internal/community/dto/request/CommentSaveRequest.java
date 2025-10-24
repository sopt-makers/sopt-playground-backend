package org.sopt.makers.internal.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentSaveRequest (
    @Schema(required = true)
    @NotBlank String content,

    @Schema(required = true)
    @NotNull Boolean isBlindWriter,

    @Schema(required = true)
    @NotNull Boolean isChildComment,

    @Schema(required = true)
    @NotNull String webLink,

    @Schema(required = false)
    Long parentCommentId,

    MentionRequest mention
) {}
