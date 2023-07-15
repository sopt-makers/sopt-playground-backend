package org.sopt.makers.internal.dto.soulmate;

import org.sopt.makers.internal.domain.soulmate.SoulmateState;

public record SoulmateMatchingResponse(
        SoulmateState state,
        Long soulmateId
) {
}
