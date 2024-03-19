package org.sopt.makers.internal.resolution.dto.response;

import java.util.List;

public record ResolutionResponse(

        String memberImageUrl,
        String memberName,
        List<String> tags,
        String content
) {
}
