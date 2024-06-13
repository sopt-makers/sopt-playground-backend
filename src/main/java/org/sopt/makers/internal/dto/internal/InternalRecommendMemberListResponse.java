package org.sopt.makers.internal.dto.internal;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record InternalRecommendMemberListResponse(

	@Schema(required = true)
	List<Long> userIds
) {}