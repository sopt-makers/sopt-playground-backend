package org.sopt.makers.internal.resolution.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResolutionSaveRequest(
	List<String> tags,
	@Schema(required = true)
	String content
) {}
