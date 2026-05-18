package org.sopt.makers.internal.community.dto.response;

import org.sopt.makers.internal.community.domain.enums.CommunityPostTag;
import org.sopt.makers.internal.member.dto.response.MemberNameAndProfileImageResponse;

public record PopularPostResponse(
	Long id,
	String title,
	MemberNameAndProfileImageResponse member,
	Integer hits,
	int likeCount,
	int commentCount,
	CommunityPostTag categoryTag,
	String categoryTagLabel
) {
}
