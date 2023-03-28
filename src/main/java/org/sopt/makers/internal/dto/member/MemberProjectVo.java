package org.sopt.makers.internal.dto.member;

public record MemberProjectVo(
        Long id,
        Integer generation,
        String name,
        String category
) {}