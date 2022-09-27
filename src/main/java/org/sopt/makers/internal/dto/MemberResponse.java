package org.sopt.makers.internal.dto;

public record MemberResponse(
    Long id,
    String name,
    Integer generation
) {}
