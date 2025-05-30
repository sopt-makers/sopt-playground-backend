package org.sopt.makers.internal.community.dto;

public record InternalCommunityPost(
		Long id,
		String title,
		String category,
		String[] images,
		Boolean isHotPost,
		String content
) { }
