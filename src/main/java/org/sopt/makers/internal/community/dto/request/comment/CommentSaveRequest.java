package org.sopt.makers.internal.community.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.sopt.makers.internal.community.dto.request.MentionRequest;

public record CommentSaveRequest (
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank String content,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull Boolean isBlindWriter,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull Boolean isChildComment,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull String webLink,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    Long parentCommentId,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    MentionRequest mention,

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    AnonymousMentionRequest anonymousMentionRequest
) {}
