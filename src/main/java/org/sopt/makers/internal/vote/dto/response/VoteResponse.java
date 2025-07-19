package org.sopt.makers.internal.vote.dto.response;

import java.util.List;

public record VoteResponse(
        Long id,
        boolean isMultiple,
        boolean hasVoted,
        int totalParticipants,
        List<VoteOptionResponse> options
) {
}
