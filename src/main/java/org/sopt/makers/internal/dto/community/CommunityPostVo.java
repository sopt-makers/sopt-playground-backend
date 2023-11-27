package org.sopt.makers.internal.dto.community;

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
        Boolean isReported,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}