package org.sopt.makers.internal.repository.community;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.community.domain.category.QCategory;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.domain.community.*;
import org.sopt.makers.internal.domain.member.QMemberBlock;
import org.sopt.makers.internal.dto.community.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommunityQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CategoryPostMemberDao> findAllPostByCursor(Integer limit, Long cursor, Long memberId, boolean filterBlockedUsers) {
        val posts = QCommunityPost.communityPost;
        val member = QMember.member;
        val category = QCategory.category;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val careers = QMemberCareer.memberCareer;
        val memberBlock = QMemberBlock.memberBlock;

        JPAQuery<CategoryPostMemberDao> query = queryFactory.select(new QCategoryPostMemberDao(posts, member, category))
                .from(posts)
                .innerJoin(posts.member, member)
                .leftJoin(member.activities, activities)
                .leftJoin(member.careers, careers)
                .innerJoin(category).on(posts.categoryId.eq(category.id))
                .where(ltPostId(cursor))
                .limit(limit)
                .distinct()
                .orderBy(posts.id.desc());

        if (filterBlockedUsers) {
            query.leftJoin(memberBlock).on(
                            memberBlock.blocker.id.eq(memberId)
                                    .and(memberBlock.blockedMember.id.eq(member.id))
                                    .and(memberBlock.isBlocked.isTrue())
                    )
                    .where(memberBlock.isNull());
        }

        return query.fetch();
    }

    public List<CategoryPostMemberDao> findAllParentCategoryPostByCursor(Long categoryId, Integer limit, Long cursor, Long memberId, boolean filterBlockedUsers) {
        val posts = QCommunityPost.communityPost;
        val member = QMember.member;
        val category = QCategory.category;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val careers = QMemberCareer.memberCareer;
        val memberBlock = QMemberBlock.memberBlock;

        JPAQuery<CategoryPostMemberDao> query = queryFactory.select(new QCategoryPostMemberDao(posts, member, category))
                .from(posts)
                .innerJoin(posts.member, member)
                .innerJoin(member.activities, activities)
                .leftJoin(member.careers, careers).on(member.id.eq(careers.memberId))
                .innerJoin(category).on(posts.categoryId.eq(category.id))
                .where(ltPostId(cursor), category.id.eq(categoryId).or(category.parent.id.eq(categoryId)))
                .limit(limit)
                .distinct()
                .orderBy(posts.id.desc());

        if (filterBlockedUsers) {
            query.leftJoin(memberBlock).on(
                            memberBlock.blocker.id.eq(memberId)
                                    .and(memberBlock.blockedMember.id.eq(member.id))
                                    .and(memberBlock.isBlocked.isTrue())
                    )
                    .where(memberBlock.isNull());
        }

        return query.fetch();
    }

    public CategoryPostMemberDao getPostById(Long postId) {
        val careers = QMemberCareer.memberCareer;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val posts = QCommunityPost.communityPost;
        val category = QCategory.category;
        val member = QMember.member;

        return queryFactory.select(new QCategoryPostMemberDao(posts, member, category))
                .from(posts)
                .innerJoin(posts.member, member)
                .leftJoin(member.activities, activities)
                .leftJoin(member.careers, careers)
                .innerJoin(category).on(posts.categoryId.eq(category.id))
                .where(posts.id.eq(postId)).distinct().fetchOne();
    }

    public List<CommentDao> findCommentByPostId(Long postId, Long memberId, boolean isBlockedOn) {
        val comment = QCommunityComment.communityComment;
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val careers = QMemberCareer.memberCareer;
        val memberBlock = QMemberBlock.memberBlock;

        JPAQuery<CommentDao> query = queryFactory.select(new QCommentDao(member, comment))
                .from(comment)
                .innerJoin(member).on(member.id.eq(comment.writerId))
                .innerJoin(member.activities, activities)
                .leftJoin(member.careers, careers)
                .where(comment.postId.eq(postId))
                .distinct()
                .orderBy(comment.id.asc());

        if (isBlockedOn) {
            query.leftJoin(memberBlock).on(
                            memberBlock.blocker.id.eq(memberId)
                                    .and(memberBlock.blockedMember.id.eq(member.id))
                                    .and(memberBlock.isBlocked.isTrue())
                    )
                    .where(memberBlock.isNull());
        }

        return query.fetch();
    }

    public CommunityPost findRecentHotPost() {
        val post = QCommunityPost.communityPost;

        return queryFactory
            .selectFrom(post)
            .where(post.isHot.eq(true))
            .orderBy(post.id.desc())
            .fetchFirst();
    }

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

    public void updateHitsByPostId(List<Long> postIdList) {
        val post = QCommunityPost.communityPost;

        queryFactory.update(post)
                .set(post.hits, post.hits.add(1))
                .where(post.id.in(postIdList))
                .execute();
    }

    public void updateIsHotByPostId(Long postId) {
        val post = QCommunityPost.communityPost;

        queryFactory.update(post)
            .set(post.isHot, true)
            .where(post.id.eq(postId))
            .execute();
    }

    private BooleanExpression ltPostId(Long cursor) {
        val posts = QCommunityPost.communityPost;
        if(cursor == null || cursor == 0) return null;
        return posts.id.lt(cursor);
    }
}
