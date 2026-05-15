package org.sopt.makers.internal.community.dto.response;

import java.util.List;

import org.sopt.makers.internal.community.domain.enums.CommunityPostListCategory;

public record PostAllResponse(
	CommunityPostListCategory category,
	Boolean hasNext,
	String nextCursor,
	List<PostResponse> posts
) {
}