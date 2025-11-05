package org.sopt.makers.internal.community.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import org.sopt.makers.internal.vote.dto.response.VoteResponse;

public record PostSaveResponse(
        @Schema(required = true)
        Long id,
        Long categoryId,
        String title,
        String content,
        Integer hits,
        List<String> images,
        Boolean isQuestion,
        Boolean isBlindWriter,
        LocalDateTime createdAt
) {}
