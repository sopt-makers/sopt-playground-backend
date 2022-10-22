package org.sopt.makers.internal.dto.member;

public record MemberResponse(
    Long id,
    String name,
    Integer generation
) {}
