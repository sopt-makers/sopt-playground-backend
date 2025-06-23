package org.sopt.makers.internal.vote.dto.request;

import java.util.List;

public record VoteSelectionRequest(
        List<Long> selectedOptions
) {
}
