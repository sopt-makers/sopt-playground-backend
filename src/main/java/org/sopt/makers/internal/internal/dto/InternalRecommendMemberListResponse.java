package org.sopt.makers.internal.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

public record InternalRecommendMemberListResponse(

	@Schema(required = true)
    Set<Long> userIds
) {}