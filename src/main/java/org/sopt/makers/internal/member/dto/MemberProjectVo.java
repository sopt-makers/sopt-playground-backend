package org.sopt.makers.internal.member.dto;

import org.springframework.aot.hint.annotation.Reflective;

@Reflective
public record MemberProjectVo(
        Long id,
        Integer generation,
        String name,
        String category
) {}