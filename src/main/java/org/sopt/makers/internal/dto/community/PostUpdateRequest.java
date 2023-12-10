package org.sopt.makers.internal.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostUpdateRequest(
    @Schema(required = true)
    Long postId,
    @Schema(required = true)
    Long categoryId,
    String title,
    String content,
    Boolean isQuestion,
    Boolean isBlindWriter,
    String[] images
) {}
