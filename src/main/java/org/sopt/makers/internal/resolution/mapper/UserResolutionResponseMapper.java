package org.sopt.makers.internal.resolution.mapper;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.dto.response.ResolutionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserResolutionResponseMapper {

    public ResolutionResponse toResolutionResponse(Member member, List<String> tags, String content) {
        return new ResolutionResponse(
                member.getProfileImage(),
                member.getName(),
                tags,
                content
        );
    }
}
