package org.sopt.makers.internal.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record PostSaveRequest(
    @Schema(required = true)
    Integer categoryId,
    @Schema(required = true)
    String content,
    String title,
    Boolean isQuestion,
    Boolean isBlindWriter,
    String[] images
) {}
