package org.sopt.makers.internal.project.dto.dao;

import com.querydsl.core.annotations.QueryProjection;

public record ProjectLinkDao (
        Long id,
        String name,
        Long linkId,
        String linkTitle,
        String linkUrl
) {
    @QueryProjection
    public ProjectLinkDao {}
}
