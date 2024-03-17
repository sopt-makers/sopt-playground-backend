package org.sopt.makers.internal.resolution.mapper;

import org.sopt.makers.internal.resolution.domain.UserResolution;
import org.sopt.makers.internal.resolution.dto.response.ResolutionSaveResponse;
import org.springframework.stereotype.Component;

@Component
public class UserResolutionResponseMapper {

	public ResolutionSaveResponse toResolutionSaveResponse(UserResolution resolution) {
		return new ResolutionSaveResponse(resolution.getId(), resolution.getContent(),
			resolution.getTagIds(), resolution.getMember().getName());
	}
}
