package org.sopt.makers.internal.dto.internal;

import java.util.List;

public record InternalLatestMemberResponse(
        List<Long> memberIds
) {
}
