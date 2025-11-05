package org.sopt.makers.internal.community.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PostUpdateResponse(
        @Schema(required = true)
        Long id,
        Long categoryId,
        String title,
        String content,
        Integer hits,
        List<String> images,
        Boolean isQuestion,
        Boolean isBlindWriter,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
