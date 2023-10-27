package org.sopt.makers.internal.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record CommentSaveRequest (
    @Schema(required = true)
    @NotBlank String content,

    @Schema(required = true)
    @NotNull Boolean isBlindWriter,

    @Schema(required = true)
    @NotNull Boolean isChildComment,

    @Schema(required = false)
    Long parentCommentId
) {}
