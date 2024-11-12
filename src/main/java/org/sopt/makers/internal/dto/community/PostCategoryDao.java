package org.sopt.makers.internal.dto.community;

import com.querydsl.core.annotations.QueryProjection;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.domain.community.CommunityPost;

public record PostCategoryDao(
		CommunityPost post,
		Category category
) {
	@QueryProjection
	public PostCategoryDao {}
}
