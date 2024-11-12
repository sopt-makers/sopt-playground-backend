package org.sopt.makers.internal.dto.community;

public record InternalCommunityPost(
		Long id,
		String title,
		String category,
		String[] images,
		Boolean isHotPost
) { }
