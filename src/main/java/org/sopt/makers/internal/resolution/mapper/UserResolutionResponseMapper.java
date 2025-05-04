package org.sopt.makers.internal.resolution.mapper;

import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.sopt.makers.internal.resolution.dto.response.ResolutionValidResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserResolutionResponseMapper {

    public ResolutionResponse toResolutionResponse(Member member, List<ResolutionTag> tags, String content) {
        return new ResolutionResponse(
            member.getProfileImage(),
            member.getName(),
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
