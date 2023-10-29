package org.sopt.makers.internal.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.QCommunityComment;
import org.sopt.makers.internal.domain.QMember;
import org.sopt.makers.internal.domain.QMemberCareer;
import org.sopt.makers.internal.domain.QMemberSoptActivity;
import org.sopt.makers.internal.domain.community.QCategory;
import org.sopt.makers.internal.domain.community.QCommunityPost;
import org.sopt.makers.internal.dto.community.CategoryPostMemberDao;
import org.sopt.makers.internal.dto.community.CommentDao;
import org.sopt.makers.internal.dto.community.QCategoryPostMemberDao;
import org.sopt.makers.internal.dto.community.QCommentDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommunityCommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CategoryPostMemberDao> findAllPostByCursor(Integer limit, Long cursor) {
        val posts = QCommunityPost.communityPost;
        return getPostQuery()
                .where(ltPostId(cursor))
                .limit(limit)
                .groupBy(posts.id)
                .orderBy(posts.createdAt.desc())
                .fetch();
    }

    public List<CommentDao> findCommentByPostId(Long postId) {
        val comment = QCommunityComment.communityComment;
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val careers = QMemberCareer.memberCareer;

        return queryFactory.select(new QCommentDao(comment.id, member.id, member.name, member.profileImage,
                        member.activities, member.careers, comment.content, comment.isBlindWriter, comment.createdAt, comment.updatedAt))
                .from(comment)
                .innerJoin(member).on(comment.writerId.eq(member.id))
                .innerJoin(member.activities, activities)
                .innerJoin(member.careers, careers).fetch();
    }

    public List<CategoryPostMemberDao> findAllParentCategoryPostByCursor(
            Long categoryId, Integer limit, Long cursor
    ) {
        val posts = QCommunityPost.communityPost;
        val category = QCategory.category;
        val member = QMember.member;

        return getPostQuery()
                .innerJoin(category).on(category.id.eq(posts.categoryId))
                .innerJoin(category.parent).on(category.parent.id.eq(categoryId))
                .where(ltPostId(cursor))
                .limit(limit)
                .groupBy(posts.id)
                .orderBy(posts.createdAt.desc())
                .fetch();
    }

    public CategoryPostMemberDao getPostById(Long postId) {
        val posts = QCommunityPost.communityPost;
        return getPostQuery().where(posts.id.eq(postId)).fetchOne();
    }

    private JPAQuery<CategoryPostMemberDao> getPostQuery() {
        val posts = QCommunityPost.communityPost;
        val comments = QCommunityComment.communityComment;
        val category = QCategory.category;
        val member = QMember.member;

        return queryFactory.select(new QCategoryPostMemberDao(posts.id, category.id, member.id, member.name,
                        member.profileImage, member.activities, member.careers, posts.title, posts.content, posts.comments.size(), posts.hits,
                        posts.isQuestion, posts.isBlindWriter, posts.images, posts.createdAt, posts.updatedAt, posts.comments))
                .from(posts)
                .innerJoin(posts.comments, comments)
                .innerJoin(posts).on(member.id.eq(posts.writerId));
    }

    private BooleanExpression ltPostId(Long postId) {
        val posts = QCommunityPost.communityPost;
        if(postId == null || postId == 0) return null;
        return posts.id.lt(postId);
    }
}