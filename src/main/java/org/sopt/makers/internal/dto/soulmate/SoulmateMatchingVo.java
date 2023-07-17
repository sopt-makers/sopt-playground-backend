package org.sopt.makers.internal.dto.soulmate;

import org.sopt.makers.internal.domain.soulmate.SoulmateState;

public record SoulmateMatchingVo(
        SoulmateState state,
        Long soulmateId
) {
}
