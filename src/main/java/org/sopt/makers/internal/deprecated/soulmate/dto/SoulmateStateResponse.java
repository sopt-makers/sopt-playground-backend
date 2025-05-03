package org.sopt.makers.internal.deprecated.soulmate.dto;

import org.sopt.makers.internal.deprecated.soulmate.domain.SoulmateState;

import java.time.LocalDateTime;

public record SoulmateStateResponse(
        Long soulmateId,
        Long opponentId,
        LocalDateTime stateModifiedAt,
        SoulmateState state,
        Integer missionSequence
) {
}
