package org.sopt.makers.internal.dto.internal;

public record InternalOfficialMemberResponse(
        Long id,
        String name,
        String profileImage,
        String introduction,
        String part,
        Integer generation
) {}
