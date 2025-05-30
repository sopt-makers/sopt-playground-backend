package org.sopt.makers.internal.deprecated.soulmate.dto;

public record MissionUpdateRequest (
    Long soulmateId,
    String message
) {}
