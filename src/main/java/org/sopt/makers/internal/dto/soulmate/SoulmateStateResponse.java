package org.sopt.makers.internal.dto.soulmate;

import org.sopt.makers.internal.domain.soulmate.SoulmateState;

import java.time.LocalDateTime;

public record SoulmateStateResponse(
        Long soulmateId,
        Long opponentId,
        LocalDateTime stateModifiedAt,
        SoulmateState state,
        Integer missionSequence
) {
}
