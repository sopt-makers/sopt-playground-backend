package org.sopt.makers.internal.dto.auth;

public record MemberRegistrationResponse (

    Boolean success,
    String name,
    Integer generation
) {}
