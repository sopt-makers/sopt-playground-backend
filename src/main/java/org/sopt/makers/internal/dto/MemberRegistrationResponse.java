package org.sopt.makers.internal.dto;

public record MemberRegistrationResponse (
    Boolean success,
    String name,
    Integer generation
) {}
