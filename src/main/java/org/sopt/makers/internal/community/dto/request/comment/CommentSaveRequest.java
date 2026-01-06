package org.sopt.makers.internal.community.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.sopt.makers.internal.community.dto.request.MentionRequest;
import org.sopt.makers.internal.exception.BadRequestException;

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
    AnonymousMentionRequest anonymousMention
) {
    public void validate() {
        validateChildCommentConsistency();
    }

    private void validateChildCommentConsistency() {
        if (isChildComment && parentCommentId == null) {
            throw new BadRequestException("답글 작성 시 부모 댓글 ID(parentCommentId)는 필수입니다.");
        }

        if (!isChildComment && parentCommentId != null) {
            throw new BadRequestException("일반 댓글 작성 시 부모 댓글 ID(parentCommentId)는 null이어야 합니다.");
        }
    }
}
