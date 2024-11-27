package org.sopt.makers.internal.community.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record PostSaveRequest(
        @Schema(required = true)
        @NotNull
        Long categoryId,

        String title,

        @Schema(required = true)
        @NotBlank
        String content,

        @Schema(required = true)
        @NotNull
        Boolean isQuestion,

        @Schema(required = true)
        @NotNull
        Boolean isBlindWriter,

        @Schema(required = true)
        @NotNull
        String[] images
) {
}
