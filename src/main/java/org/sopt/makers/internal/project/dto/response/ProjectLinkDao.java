package org.sopt.makers.internal.project.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
