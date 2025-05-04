package org.sopt.makers.internal.member.dto;

public record MemberProjectVo(
        Long id,
        Integer generation,
        String name,
        String category
) {}