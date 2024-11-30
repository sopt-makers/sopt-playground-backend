package org.sopt.makers.internal.dto.community;

import org.sopt.makers.internal.community.domain.CommunityPost;

public record HotPostResponse(
	Long id,
	String title,
	String content
) {
	public static HotPostResponse of(CommunityPost post) {
		return new HotPostResponse(post.getId(), post.getTitle(), post.getContent());
	}
}
