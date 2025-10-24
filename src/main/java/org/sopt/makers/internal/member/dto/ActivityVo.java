package org.sopt.makers.internal.member.dto;

import org.springframework.aot.hint.annotation.Reflective;

@Reflective
public record ActivityVo(
        Long id,
        Integer generation,
        String team,
        String part,
        boolean isProject
) {
}
