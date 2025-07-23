package org.sopt.makers.internal.resolution.mapper;

import java.util.List;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.sopt.makers.internal.resolution.dto.response.ResolutionValidResponse;
import org.springframework.stereotype.Component;

@Component
public class UserResolutionResponseMapper {

    public ResolutionResponse toResolutionResponse(Boolean hasWritten, List<ResolutionTag> tags, String content, Boolean hasDrawn) {
        return new ResolutionResponse(
                hasWritten,
                tags,
                content,
                hasDrawn
        );
    }

    public ResolutionValidResponse toResolutionValidResponse(boolean isRegistration) {
        return new ResolutionValidResponse(
            isRegistration
        );
    }
}
