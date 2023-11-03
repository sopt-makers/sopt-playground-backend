package org.sopt.makers.internal.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.community.Category;
import org.sopt.makers.internal.domain.community.QCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Category> getCategoryList() {
        val category = QCategory.category;
        return queryFactory.selectFrom(category)
                .leftJoin(category.parent)
                .orderBy(category.parent.id.asc().nullsFirst(), category.id.asc())
                .fetch();
    }
}
