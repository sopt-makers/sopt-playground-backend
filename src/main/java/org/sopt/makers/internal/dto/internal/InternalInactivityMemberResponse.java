package org.sopt.makers.internal.dto.internal;

import java.util.List;

public record InternalInactivityMemberResponse(
        List<Long> memberIds
) {
}
