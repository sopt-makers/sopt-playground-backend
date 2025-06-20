package org.sopt.makers.internal.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.sopt.makers.internal.vote.dto.request.VoteRequest;

@Builder
public record PostSaveRequest(
        @Schema(required = true)
        @NotNull(message = "카테고리 id는 필수 입력값입니다.")
        Long categoryId,

        String title,

        @Schema(required = true)
        @NotBlank(message = "게시글 본문은 공백일 수 없습니다.")
        String content,

        @Schema(required = true)
        @NotNull(message = "질문글 여부 필드는 필수 입력값입니다.")
        Boolean isQuestion,

        @Schema(required = true)
        @NotNull(message = "익명글 여부 필드는 필수 입력값입니다.")
        Boolean isBlindWriter,

        @Schema(required = true)
        @NotNull(message = "이미지 필드는 필수 필드입니다.")
        String[] images,

        String link,

        VoteRequest vote
) {
}
