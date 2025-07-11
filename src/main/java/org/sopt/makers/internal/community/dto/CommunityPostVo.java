package org.sopt.makers.internal.community.dto;

import org.sopt.makers.internal.vote.dto.response.VoteResponse;

import java.time.LocalDateTime;

public record CommunityPostVo(
        Long id,
        Long categoryId,
        String title,
        String content,
        Integer hits,
        String[] images,
        Boolean isQuestion,
        Boolean isBlindWriter,
        String sopticleUrl,
        Boolean isReported,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        VoteResponse vote
) {}