package org.sopt.makers.internal.resolution.dto.response;

public record ResolutionSaveResponse(
	Long id,
	String content,
	String tagIds,
	String userName
) { }
