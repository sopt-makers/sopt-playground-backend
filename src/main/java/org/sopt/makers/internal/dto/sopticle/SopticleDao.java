package org.sopt.makers.internal.dto.sopticle;

import com.querydsl.core.annotations.QueryProjection;

public record SopticleDao (
        Long id,
        String link,
        Long memberId,
        String name,
        String profileImage,
        String part,
        Integer generation
){
    @QueryProjection
    public SopticleDao {}
}
