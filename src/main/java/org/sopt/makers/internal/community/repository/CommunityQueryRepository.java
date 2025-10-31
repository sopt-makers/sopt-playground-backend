package org.sopt.makers.internal.community.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.QCommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.anonymous.QAnonymousProfile;
import org.sopt.makers.internal.community.domain.category.QCategory;
import org.sopt.makers.internal.community.domain.comment.QCommunityComment;
import org.sopt.makers.internal.community.dto.CategoryPostMemberDao;
import org.sopt.makers.internal.community.dto.CommentDao;
import org.sopt.makers.internal.member.domain.QMember;
import org.sopt.makers.internal.member.domain.QMemberBlock;
import org.sopt.makers.internal.member.domain.QMemberCareer;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommunityQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final long CATEGORY_PART_TALK = 2L;
    private static final long CATEGORY_PROMOTION = 4L;

    public List<CategoryPostMemberDao> findAllParentCategoryPostByCursor(Long categoryId, Integer limit, Long cursor, Long memberId, boolean filterBlockedUsers) {
        val posts = QCommunityPost.communityPost;
        val member = QMember.member;
        val category = QCategory.category;
        val careers = QMemberCareer.memberCareer;
        val memberBlock = QMemberBlock.memberBlock;

        JPAQuery<CategoryPostMemberDao> query;
        if (categoryId == CATEGORY_PART_TALK || categoryId == CATEGORY_PROMOTION) {
            query = queryFactory.select(Projections.constructor(CategoryPostMemberDao.class, posts, member, category))
                    .from(posts)
                    .innerJoin(posts.member, member)
                    .leftJoin(member.careers, careers).on(member.id.eq(careers.memberId))
                    .innerJoin(category).on(posts.categoryId.eq(category.id))
                    .where(ltPostId(cursor), category.id.eq(categoryId).or(category.parent.id.eq(categoryId)))
                    .limit(limit)
                    .distinct()
                    .orderBy(posts.createdAt.desc());
        } else {
            query = queryFactory.select(Projections.constructor(CategoryPostMemberDao.class, posts, member, category))
                    .from(posts)
                    .innerJoin(posts.member, member)
                    .leftJoin(member.careers, careers).on(member.id.eq(careers.memberId))
                    .innerJoin(category).on(posts.categoryId.eq(category.id))
                    .where(ltPostId(cursor), category.id.eq(categoryId).or(category.parent.id.eq(categoryId)))
                    .limit(limit)
                    .distinct()
                    .orderBy(category.displayOrder.asc(), posts.createdAt.desc());
        }

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
        val posts = QCommunityPost.communityPost;
        val category = QCategory.category;
        val member = QMember.member;

        return queryFactory.select(Projections.constructor(CategoryPostMemberDao.class, posts, member, category))
                .from(posts)
                .innerJoin(posts.member, member)
                .innerJoin(category).on(posts.categoryId.eq(category.id))
                .where(posts.id.eq(postId)).distinct().fetchOne();
    }

    public Map<Long, String> getCategoryNamesByIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QCategory category = QCategory.category;

        return queryFactory
                .select(category.id, category.name)
                .from(category)
                .where(category.id.in(categoryIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(category.id),
                        tuple -> tuple.get(category.name) // category.name은 NOT NULL 보장
                ));
    }

    public Map<Long, AnonymousProfile> getAnonymousProfilesByPostId(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QAnonymousProfile anonymousProfile = QAnonymousProfile.anonymousProfile;

        return queryFactory
                .selectFrom(anonymousProfile)
                .leftJoin(anonymousProfile.post).fetchJoin()
                .leftJoin(anonymousProfile.nickname).fetchJoin()
                .leftJoin(anonymousProfile.profileImg).fetchJoin()
                .where(anonymousProfile.post.id.in(postIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        profile -> profile.getPost().getId(),
                        profile -> profile
                ));
    }

    public List<CommentDao> findCommentByPostId(Long postId, Long memberId, boolean isBlockedOn) {
        val comment = QCommunityComment.communityComment;
        val member = QMember.member;
        val careers = QMemberCareer.memberCareer;
        val memberBlock = QMemberBlock.memberBlock;
        val anonymousProfile = QAnonymousProfile.anonymousProfile;

        JPAQuery<CommentDao> query = queryFactory.select(Projections.constructor(CommentDao.class, member, comment))
                .from(comment)
                .innerJoin(member).on(member.id.eq(comment.writerId))
                .leftJoin(member.careers, careers)
                .leftJoin(comment.anonymousProfile, anonymousProfile).fetchJoin()
                .leftJoin(anonymousProfile.nickname).fetchJoin()
                .leftJoin(anonymousProfile.profileImg).fetchJoin()
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
            .orderBy(post.createdAt.desc())
            .fetchFirst();
    }

    public void updateIsHotByPostId(Long postId) {
        val post = QCommunityPost.communityPost;

        queryFactory.update(post)
            .set(post.isHot, true)
            .where(post.id.eq(postId))
            .execute();
    }

    public List<CommunityPost> findPopularPosts(int limitCount) {
        QCommunityPost communityPost = QCommunityPost.communityPost;
        QMember member = QMember.member;

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        return queryFactory
                .selectFrom(communityPost)
                .leftJoin(communityPost.member, member).fetchJoin()
                .where(communityPost.createdAt.after(oneMonthAgo))
                .orderBy(communityPost.hits.desc())
                .limit(limitCount)
                .fetch();
    }
    
    private BooleanExpression ltPostId(Long cursor) {
        val posts = QCommunityPost.communityPost;
        if(cursor == null || cursor == 0) return null;
        return posts.id.lt(cursor);
    }
}
