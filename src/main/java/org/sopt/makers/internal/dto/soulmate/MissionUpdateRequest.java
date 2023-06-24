package org.sopt.makers.internal.dto.soulmate;

public record MissionUpdateRequest (
    Long soulmateId,
    Long senderId,
    Integer missionSequence,
    String message
) {}
