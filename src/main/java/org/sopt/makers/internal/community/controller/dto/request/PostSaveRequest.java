package org.sopt.makers.internal.community.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostSaveRequest(
    @Schema(required = true)
    Long categoryId,

    String title,

    @Schema(required = true)
    String content,

    @Schema(required = true)
    Boolean isQuestion,

    @Schema(required = true)
    Boolean isBlindWriter,

    String[] images
) {}
