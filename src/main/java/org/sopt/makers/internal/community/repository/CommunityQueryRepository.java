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
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.dto.CategoryPostMemberDao;
import org.sopt.makers.internal.community.dto.CommunityDbCursor;
import org.sopt.makers.internal.community.dto.comment.CommentDao;
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

    public List<CategoryPostMemberDao> findPostsByCategoryCodes(
        List<CommunityCategoryCode> categoryCodes,
        CommunityDbCursor cursor,
        LocalDateTime snapshotTime,
        Integer limit,
        Long memberId,
        boolean filterBlockedUsers
    ) {
        val post = QCommunityPost.communityPost;
        val member = QMember.member;
        val category = QCategory.category;
        val memberBlock = QMemberBlock.memberBlock;

        JPAQuery<CategoryPostMemberDao> query = queryFactory
            .select(Projections.constructor(CategoryPostMemberDao.class, post, member, category))
            .from(post)
            .innerJoin(post.member, member)
            .innerJoin(post.category, category)
            .where(
                category.code.in(categoryCodes),
                post.createdAt.loe(snapshotTime),
                ltCursor(cursor)
            )
            .limit(limit)
            .orderBy(post.createdAt.desc(), post.id.desc());

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
        val post = QCommunityPost.communityPost;
        val category = QCategory.category;
        val member = QMember.member;

        return queryFactory.select(Projections.constructor(CategoryPostMemberDao.class, post, member, category))
            .from(post)
            .innerJoin(post.member, member)
            .innerJoin(post.category, category)
            .where(post.id.eq(postId))
            .fetchOne();
    }

    public Map<Long, AnonymousProfile> getAnonymousProfilesByPostId(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QAnonymousProfile anonymousProfile = QAnonymousProfile.anonymousProfile;
        QCommunityPost post = QCommunityPost.communityPost;

        return queryFactory
            .selectFrom(anonymousProfile)
            .join(anonymousProfile.post, post).fetchJoin()
            .leftJoin(anonymousProfile.nickname).fetchJoin()
            .leftJoin(anonymousProfile.profileImg).fetchJoin()
            .where(
                anonymousProfile.post.id.in(postIds),
                anonymousProfile.member.id.eq(post.member.id)
            )
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
        val memberBlock = QMemberBlock.memberBlock;
        val anonymousProfile = QAnonymousProfile.anonymousProfile;

        JPAQuery<CommentDao> query = queryFactory.select(Projections.constructor(CommentDao.class, member, comment))
            .from(comment)
            .innerJoin(member).on(member.id.eq(comment.writerId))
            .leftJoin(comment.anonymousProfile, anonymousProfile).fetchJoin()
            .leftJoin(anonymousProfile.nickname).fetchJoin()
            .leftJoin(anonymousProfile.profileImg).fetchJoin()
            .where(comment.postId.eq(postId))
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

    // 조회수 기준 인기 게시글 - 현재 앱팀 internal api에 사용중
    public List<CommunityPost> findPopularPosts(int limitCount) {
        QCommunityPost communityPost = QCommunityPost.communityPost;
        QMember member = QMember.member;
        QCategory category = QCategory.category;
        QCategory parentCategory = new QCategory("parentCategory");

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        return queryFactory
            .selectFrom(communityPost)
            .leftJoin(communityPost.member, member).fetchJoin()
            .leftJoin(communityPost.category, category).fetchJoin()
            .leftJoin(category.parent, parentCategory).fetchJoin()
            .where(communityPost.createdAt.after(oneMonthAgo))
            .orderBy(communityPost.hits.desc())
            .limit(limitCount)
            .fetch();
    }

    // 인기글 후보용 최근 1달 게시글 조회 - 38기부터 인기글 조회에 사용중
    public List<CommunityPost> findPopularCandidatePosts(LocalDateTime since) {
        QCommunityPost communityPost = QCommunityPost.communityPost;
        QMember member = QMember.member;
        QCategory category = QCategory.category;
        QCategory parentCategory = new QCategory("parentCategory");

        return queryFactory
            .selectFrom(communityPost)
            .leftJoin(communityPost.member, member).fetchJoin()
            .leftJoin(communityPost.category, category).fetchJoin()
            .leftJoin(category.parent, parentCategory).fetchJoin()
            .where(
                communityPost.createdAt.goe(since),
                communityPost.isReported.isFalse()
            )
            .orderBy(communityPost.createdAt.desc(), communityPost.id.desc())
            .fetch();
    }

    public List<CommentDao> findCommentsByPostIds(
        List<Long> postIds,
        Long memberId,
        boolean isBlockedOn
    ) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }

        val comment = QCommunityComment.communityComment;
        val member = QMember.member;
        val memberBlock = QMemberBlock.memberBlock;
        val anonymousProfile = QAnonymousProfile.anonymousProfile;

        JPAQuery<CommentDao> query = queryFactory
            .select(Projections.constructor(CommentDao.class, member, comment))
            .from(comment)
            .innerJoin(member).on(member.id.eq(comment.writerId))
            .leftJoin(comment.anonymousProfile, anonymousProfile).fetchJoin()
            .leftJoin(anonymousProfile.nickname).fetchJoin()
            .leftJoin(anonymousProfile.profileImg).fetchJoin()
            .where(comment.postId.in(postIds))
            .orderBy(comment.postId.asc(), comment.id.asc());

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

    private BooleanExpression ltCursor(CommunityDbCursor cursor) {
        val post = QCommunityPost.communityPost;

        if (cursor == null || cursor.createdAt() == null || cursor.postId() == null) {
            return null;
        }

        return post.createdAt.lt(cursor.createdAt())
            .or(post.createdAt.eq(cursor.createdAt()).and(post.id.lt(cursor.postId())));
    }
}