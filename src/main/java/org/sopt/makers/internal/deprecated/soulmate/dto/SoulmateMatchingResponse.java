package org.sopt.makers.internal.deprecated.soulmate.dto;

import org.sopt.makers.internal.deprecated.soulmate.domain.SoulmateState;

public record SoulmateMatchingResponse(
        SoulmateState state,
        Long soulmateId
) {
}
