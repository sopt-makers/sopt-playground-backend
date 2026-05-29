package org.sopt.makers.internal.community.repository.post;

import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.sopt.makers.internal.community.dto.PostCategoryDao;

public interface CommunityPostRepositoryCustom {

	PostCategoryDao findRecentPostByCategoryGroup(CommunityCategoryGroup categoryGroup);
}
