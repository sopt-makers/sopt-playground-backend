package org.sopt.makers.internal.dto.soulmate;

public record MissionUpdateRequest (
    Long soulmateId,
    String message
) {}
