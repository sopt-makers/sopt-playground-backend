package org.sopt.makers.internal.resolution.dto.response;

import org.sopt.makers.internal.resolution.domain.ResolutionTag;

import java.util.List;

public record ResolutionResponse(

        String memberImageUrl,
        String memberName,
        List<ResolutionTag> tags,
        String content
) {
}
