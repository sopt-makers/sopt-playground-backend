package org.sopt.makers.internal.community.repository.post;

import org.sopt.makers.internal.community.dto.PostCategoryDao;

public interface CommunityPostRepositoryCustom {

	PostCategoryDao findRecentPostByCategory(String categoryName);
}
