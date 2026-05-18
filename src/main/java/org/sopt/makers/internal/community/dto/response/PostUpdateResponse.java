package org.sopt.makers.internal.community.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;

public record PostUpdateResponse(
        @Schema(required = true)
        Long id,
        CommunityCategoryCode code,
        String title,
        String content,
        Integer hits,
        List<String> images,
        Boolean isBlindWriter,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
