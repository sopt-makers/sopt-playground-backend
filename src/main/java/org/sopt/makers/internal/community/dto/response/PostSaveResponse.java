package org.sopt.makers.internal.community.dto.response;

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
        String[] images,
        Boolean isQuestion,
        Boolean isBlindWriter,
        LocalDateTime createdAt,
        VoteResponse vote
) {}
