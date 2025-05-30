package org.sopt.makers.internal.internal.dto;

public record InternalOfficialMemberResponse(
        Long id,
        String name,
        String profileImage,
        String introduction,
        String part,
        Integer generation,
        boolean allowOfficial
) {}
