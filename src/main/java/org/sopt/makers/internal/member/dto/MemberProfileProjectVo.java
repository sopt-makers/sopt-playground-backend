package org.sopt.makers.internal.member.dto;

import java.util.List;

public record MemberProfileProjectVo(
        Long id,
        Integer generation,
        String part,
        String team,
        List<MemberProjectVo> projects
) {}