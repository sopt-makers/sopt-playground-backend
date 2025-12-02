package org.sopt.makers.internal.community.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "댓글 내용은 필수입니다")
        String content
) {
}
