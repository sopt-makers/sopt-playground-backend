package org.sopt.makers.internal.member.dto;

import org.springframework.aot.hint.annotation.Reflective;

import java.util.List;

@Reflective
public record MemberProfileProjectVo(
        Long id,
        Integer generation,
        String part,
        String team,
        List<MemberProjectVo> projects
) {}