package org.sopt.makers.internal.community.dto.request.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.sopt.makers.internal.community.dto.request.MentionRequest;

public record CommentUpdateRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "댓글 내용은 필수입니다")
        String content,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "익명 여부는 필수입니다")
        Boolean isBlindWriter,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "웹 링크는 필수입니다")
        String webLink,

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        MentionRequest mention,

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        AnonymousMentionRequest anonymousMention
) {
}
