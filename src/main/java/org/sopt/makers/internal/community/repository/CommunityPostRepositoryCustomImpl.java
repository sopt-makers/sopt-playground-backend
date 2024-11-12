package org.sopt.makers.internal.community.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.category.QCategory;
import org.sopt.makers.internal.domain.community.QCommunityPost;
import org.sopt.makers.internal.dto.community.PostCategoryDao;
import org.sopt.makers.internal.dto.community.QPostCategoryDao;

@RequiredArgsConstructor
public class CommunityPostRepositoryCustomImpl implements CommunityPostRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public PostCategoryDao findRecentPostByCategory(String categoryName) {
		QCommunityPost posts = QCommunityPost.communityPost;
		QCategory category = QCategory.category;

		return queryFactory
				.select(new QPostCategoryDao(posts, category))
				.from(posts)
				.innerJoin(category).on(posts.categoryId.eq(category.id))
				.where(category.name.eq(categoryName))
				.orderBy(posts.id.desc())
				.fetchFirst();
	}
}
