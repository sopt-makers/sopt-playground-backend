package org.sopt.makers.internal.dto.member;

import com.querydsl.core.annotations.QueryProjection;

public record MemberProfileProjectDao(
        Long id,
        Long writerId,
        String name,
        String summary,
        Integer generation,
        String category,
        String logoImage,
        String thumbnailImage
) {
    @QueryProjection
    public MemberProfileProjectDao {}
}
