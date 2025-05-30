package org.sopt.makers.internal.community.repository.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.QCommunityPost;
import org.sopt.makers.internal.community.domain.category.QCategory;
import org.sopt.makers.internal.community.dto.PostCategoryDao;
import org.sopt.makers.internal.community.dto.QPostCategoryDao;

@RequiredArgsConstructor
public class CommunityPostRepositoryCustomImpl implements CommunityPostRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	private BooleanExpression checkCategoryEqualsName(String categoryName) {
		if (categoryName == null) return null;
		return QCategory.category.name.eq(categoryName);
	}

	@Override
	public PostCategoryDao findRecentPostByCategory(String categoryName) {
		QCommunityPost posts = QCommunityPost.communityPost;
		QCategory category = QCategory.category;

		return queryFactory
				.select(new QPostCategoryDao(posts, category))
				.from(posts)
				.innerJoin(category).on(posts.categoryId.eq(category.id))
				.where(checkCategoryEqualsName(categoryName))
				.orderBy(posts.id.desc())
				.fetchFirst();
	}
}
