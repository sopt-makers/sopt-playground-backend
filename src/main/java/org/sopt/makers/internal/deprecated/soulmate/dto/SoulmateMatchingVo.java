package org.sopt.makers.internal.deprecated.soulmate.dto;

import org.springframework.aot.hint.annotation.Reflective;
import org.sopt.makers.internal.deprecated.soulmate.domain.SoulmateState;

@Reflective
public record SoulmateMatchingVo(
        SoulmateState state,
        Long soulmateId
) {
}
