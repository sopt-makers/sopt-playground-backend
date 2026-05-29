package org.sopt.makers.internal.community.dto.response;

import org.sopt.makers.internal.community.domain.enums.CommunityPostTag;

public record RecentPostResponse(
	Long id,
	String title,
	String content,
	String createdAt,
	int likeCount,
	int commentCount,
	CommunityPostTag categoryTag,
	String categoryTagLabel,
	Integer totalVoteCount
) {
}