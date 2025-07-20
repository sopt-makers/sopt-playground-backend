package org.sopt.makers.internal.resolution.mapper;

import java.util.List;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.sopt.makers.internal.resolution.dto.response.ResolutionValidResponse;
import org.springframework.stereotype.Component;

@Component
public class UserResolutionResponseMapper {

    public ResolutionResponse toResolutionResponse(InternalUserDetails userDetails, List<ResolutionTag> tags, String content) {
        return new ResolutionResponse(
                userDetails.profileImage(),
                userDetails.name(),
                tags,
                content
        );
    }

    public ResolutionValidResponse toResolutionValidResponse(boolean isRegistration) {
        return new ResolutionValidResponse(
            isRegistration
        );
    }
}
