package org.sopt.makers.internal.community.dto.request;

import java.util.List;

import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

public record PostUpdateRequest(
        @Schema(required = true)
        @NotNull
        Long postId,

        @Schema(required = true)
        @NotNull
		CommunityCategoryCode categoryCode,

        String title,
        String content,
        Boolean isBlindWriter,
        List<String> images,
        String link,

        MentionRequest mention
) { }
