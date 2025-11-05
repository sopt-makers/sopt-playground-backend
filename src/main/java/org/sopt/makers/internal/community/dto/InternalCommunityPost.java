package org.sopt.makers.internal.community.dto;

import java.util.List;

public record InternalCommunityPost(
		Long id,
		String title,
		String category,
		List<String> images,
		Boolean isHotPost,
		String content
) { }
