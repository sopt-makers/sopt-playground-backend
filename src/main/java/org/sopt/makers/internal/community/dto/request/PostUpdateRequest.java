package org.sopt.makers.internal.community.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public record PostUpdateRequest(
        @Schema(required = true)
        @NotNull
        Long postId,

        @Schema(required = true)
        @NotNull
        Long categoryId,

        String title,
        String content,
        Boolean isQuestion,
        Boolean isBlindWriter,
        String[] images,
        String link,

        MentionRequest mention
) { }
