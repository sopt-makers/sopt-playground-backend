package org.sopt.makers.internal.community.dto;

import com.querydsl.core.annotations.QueryProjection;
import org.springframework.aot.hint.annotation.Reflective;
import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.CommunityPost;

@Reflective
public record PostCategoryDao(
		CommunityPost post,
		Category category
) {
	@QueryProjection
	public PostCategoryDao {}
}
