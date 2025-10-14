package org.sopt.makers.internal.member.dto;

import org.springframework.aot.hint.annotation.Reflective;

@Reflective
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
    // @QueryProjection 제거하고 Projections.constructor() 사용
}
