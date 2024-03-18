package org.sopt.makers.internal.resolution.dto.response;

public record ResolutionResponse(

        String memberImageUrl,
        String memberName,
        String tags,
        String content
) {
}
