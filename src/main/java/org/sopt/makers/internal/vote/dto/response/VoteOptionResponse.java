package org.sopt.makers.internal.vote.dto.response;

public record VoteOptionResponse(
        Long id,
        String content,
        int voteCount,
        int votePercent,
        boolean isSelected
) {
}
