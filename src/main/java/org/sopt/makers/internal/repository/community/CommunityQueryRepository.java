package org.sopt.makers.internal.repository.community;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.*;
import org.sopt.makers.internal.domain.community.*;
import org.sopt.makers.internal.dto.community.CategoryPostMemberDao;
import org.sopt.makers.internal.dto.community.CommentDao;
import org.sopt.makers.internal.dto.community.QCategoryPostMemberDao;
import org.sopt.makers.internal.dto.community.QCommentDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommunityQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CategoryPostMemberDao> findAllPostByCursor(Integer limit, Long cursor) {
        val posts = QCommunityPost.communityPost;
        val careers = QMemberCareer.memberCareer;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val category = QCategory.category;
        val member = QMember.member;

        return queryFactory.select(new QCategoryPostMemberDao(posts, member, category))
                .from(posts)
                .innerJoin(posts.member, member)
                .leftJoin(member.activities, activities)
                .leftJoin(member.careers, careers)
                .innerJoin(category).on(posts.categoryId.eq(category.id))
                .where(ltPostId(cursor))
                .limit(limit)
                .distinct()
                .orderBy(posts.createdAt.desc())
                .fetch();
    }

    public List<CategoryPostMemberDao> findAllParentCategoryPostByCursor(
            Long categoryId, Integer limit, Long cursor
    ) {
        val member = QMember.member;
        val careers = QMemberCareer.memberCareer;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val posts = QCommunityPost.communityPost;
        val category = QCategory.category;

        return queryFactory.select(new QCategoryPostMemberDao(posts, member, category))
                .from(posts)
                .innerJoin(posts.member, member)
                .innerJoin(member.activities, activities)
                .leftJoin(member.careers, careers).on(member.id.eq(careers.memberId))
                .innerJoin(category).on(posts.categoryId.eq(category.id))
                .where(ltPostId(cursor), category.id.eq(categoryId).or(category.parent.id.eq(categoryId)))
                .limit(limit)
                .distinct()
                .orderBy(posts.createdAt.desc())
                .fetch();
    }

    public CategoryPostMemberDao getPostById(Long postId) {
        val posts = QCommunityPost.communityPost;
        return getPostQuery().where(posts.id.eq(postId)).fetchOne();
    }

    private JPAQuery<CategoryPostMemberDao> getPostQuery() {
        val posts = QCommunityPost.communityPost;
        val member = QMember.member;
        val careers = QMemberCareer.memberCareer;
        val activities = QMemberSoptActivity.memberSoptActivity;

        return queryFactory.select(new QCategoryPostMemberDao(member, posts))
                .from(posts)
                .innerJoin(member).on(member.id.eq(posts.writerId))
                .innerJoin(member.activities, activities)
                .innerJoin(member.careers, careers)
                .groupBy(member.id, posts.id);
    }

    private BooleanExpression ltPostId(Long cursor) {
        val posts = QCommunityPost.communityPost;
        if(cursor == null || cursor == 0) return null;
        return posts.id.lt(cursor);
    }

    public List<CommentDao> findCommentByPostId(Long postId) {
        //TODO: 계층형 댓글 조회로 변경
        val comment = QCommunityComment.communityComment;
        val member = QMember.member;
        val activities = QMemberSoptActivity.memberSoptActivity;
        val careers = QMemberCareer.memberCareer;

        return queryFactory.select(new QCommentDao(member, comment))
                .from(comment)
                .innerJoin(member).on(member.id.eq(comment.writerId))
                .innerJoin(member.activities, activities)
                .innerJoin(member.careers, careers)
                .where(comment.postId.eq(postId))
                .groupBy(comment.id, member.id)
                .fetch();
    }
}
