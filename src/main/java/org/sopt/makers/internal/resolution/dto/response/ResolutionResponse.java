package org.sopt.makers.internal.resolution.dto.response;

import java.util.List;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;

public record ResolutionResponse(
        boolean hasWrittenTimeCapsule,
        List<ResolutionTag> tags,
        String content,
        boolean hasDrawnLuckyPick
) {
}
