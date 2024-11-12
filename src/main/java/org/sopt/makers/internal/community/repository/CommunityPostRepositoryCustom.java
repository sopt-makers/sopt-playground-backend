package org.sopt.makers.internal.community.repository;

import org.sopt.makers.internal.dto.community.PostCategoryDao;

public interface CommunityPostRepositoryCustom {

	PostCategoryDao findRecentPostByCategory(String categoryName);
}
