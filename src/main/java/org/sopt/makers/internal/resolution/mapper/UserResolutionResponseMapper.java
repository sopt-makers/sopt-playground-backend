package org.sopt.makers.internal.resolution.mapper;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.springframework.stereotype.Component;

@Component
public class UserResolutionResponseMapper {

    public ResolutionResponse toResolutionResponse(Member member, UserResolution userResolution) {
        return new ResolutionResponse(
                member.getProfileImage(),
                member.getName(),
                userResolution.getTagIds(),
                userResolution.getContent()
        );
    }
}
