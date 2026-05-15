package org.sopt.makers.internal.community.repository.post;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.QCommunityPost;
import org.sopt.makers.internal.community.domain.category.QCategory;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;
import org.sopt.makers.internal.community.dto.PostCategoryDao;

@RequiredArgsConstructor
public class CommunityPostRepositoryCustomImpl implements CommunityPostRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public PostCategoryDao findRecentPostByCategoryGroup(CommunityCategoryGroup categoryGroup) {
		QCommunityPost post = QCommunityPost.communityPost;
		QCategory category = QCategory.category;

		return queryFactory
			.select(Projections.constructor(PostCategoryDao.class, post, category))
			.from(post)
			.innerJoin(post.category, category)
			.where(category.categoryGroup.eq(categoryGroup))
			.orderBy(post.createdAt.desc(), post.id.desc())
			.fetchFirst();
	}
}