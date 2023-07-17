package org.sopt.makers.internal.dto.soulmate;

import org.sopt.makers.internal.domain.soulmate.SoulmateState;

public record MissionResponse(
        SoulmateState state,
        String hint
) {
}
