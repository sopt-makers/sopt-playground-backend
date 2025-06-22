package org.sopt.makers.internal.vote.dto.request;

import java.util.List;
public record VoteRequest(
        boolean isMultiple,
        List<String> voteOptions
) {
}
