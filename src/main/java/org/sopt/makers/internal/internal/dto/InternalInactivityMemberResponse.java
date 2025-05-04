package org.sopt.makers.internal.internal.dto;

import java.util.List;

public record InternalInactivityMemberResponse(
        List<Long> memberIds
) {
}
