package org.sopt.makers.internal.member.dto;

import com.querydsl.core.annotations.QueryProjection;

public record MemberProfileProjectDao(
        Long id,
        Long writerId,
        String name,
        String summary,
        Integer generation,
        String category,
        String logoImage,
        String thumbnailImage,
        String[] serviceType
) {
    @QueryProjection
    public MemberProfileProjectDao {}
}
