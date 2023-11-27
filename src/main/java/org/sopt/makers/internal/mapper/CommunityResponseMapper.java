package org.sopt.makers.internal.mapper;

import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.community.Category;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.sopt.makers.internal.dto.community.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CommunityResponseMapper {
    public CommentResponse toCommentResponse(CommentDao dao, Long memberId) {
        val member = dao.comment().getIsBlindWriter() ? null : toMemberResponse(dao.member());
        val comment = dao.comment();
        val isMine = Objects.equals(dao.member().getId(), memberId);
        return new CommentResponse(comment.getId(), member, isMine, comment.getPostId(), comment.getParentCommentId(),
                comment.getContent(), comment.getIsBlindWriter(), comment.getIsReported(), comment.getCreatedAt());
    }

    public CommunityPostMemberVo toCommunityVo(CategoryPostMemberDao dao) {
        val member = toMemberResponse(dao.member());
        val category = toCategoryResponse(dao.category());
        val post = toPostVo(dao.posts());
        return new CommunityPostMemberVo(member, post, category);
    }

    public PostSaveResponse toPostSaveResponse(CommunityPost post) {
        return new PostSaveResponse(post.getId(), post.getCategoryId(), post.getTitle(),
                post.getContent(), post.getHits(), post.getImages(), post.getIsQuestion(),
                post.getIsBlindWriter(), post.getCreatedAt());
    }

    public PostDetailResponse toPostDetailReponse(CommunityPostMemberVo post, Long memberId) {
        val member = post.post().isBlindWriter() ? null : post.member();
        val isMine = Objects.equals(post.member().id(), memberId);
        return new PostDetailResponse(member, post.post(), post.category(), isMine);
    }

    public MemberVo toMemberResponse(Member member) {
        if(member == null) return null;
        val career = (member.getCareers() == null || member.getCareers().stream().noneMatch(MemberCareer::getIsCurrent)) ?
                null : member.getCareers().stream().filter(MemberCareer::getIsCurrent).toList().get(0);
        member.getActivities().sort((act1, act2) -> (act2.getGeneration() - act1.getGeneration()));
        return new MemberVo(member.getId(),member.getName(), member.getProfileImage(),
                member.getActivities().get(0), career);
    }

    public CategoryVo toCategoryResponse(Category category) {
        if(category == null) return null;
        val parentCategoryName = category.getParent() == null ? null : category.getParent().getName();
        val parentId = category.getParent() == null ? null : category.getParent().getId();
        return new CategoryVo(category.getId(), category.getName(), parentId, parentCategoryName);
    }

    public CommunityPostVo toPostVo(CommunityPost post) {
        return new CommunityPostVo(post.getId(), post.getCategoryId(), post.getTitle(), post.getContent(), post.getHits(),
                post.getImages(), post.getIsQuestion(), post.getIsBlindWriter(), post.getIsReported(), post.getCreatedAt(), post.getUpdatedAt());
    }

    public PostResponse toPostResponse (CommunityPostMemberVo dao, List<CommentDao> commentDaos, Long memberId) {
        val post = dao.post();
        val category = dao.category();
        val member = dao.post().isBlindWriter() ? null : dao.member();
        val writerId = dao.post().isBlindWriter() ? null : dao.member().id();
        val isMine = Objects.equals(dao.member().id(), memberId);
        val comments = commentDaos.stream().map(comment -> toCommentResponse(comment, memberId)).collect(Collectors.toList());
        return new PostResponse(post.id(), member, writerId, isMine, post.categoryId(), category.name(), post.title(), post.content(), post.hits(),
                comments.size(), post.images(), post.isQuestion(), post.isBlindWriter(), post.createdAt(), comments);
    }
}
