package org.sopt.makers.internal.mapper;

import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.domain.community.Category;
import org.sopt.makers.internal.dto.community.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CommunityResponseMapper {
    public CommentResponse toCommentResponse(CommentDao dao) {
        val member = dao.comment().getIsBlindWriter() ? null : toMemberResponse(dao.member());
        val comment = dao.comment();
        return new CommentResponse(comment.getId(), member, comment.getPostId(), comment.getParentCommentId(),
                comment.getContent(), comment.getIsBlindWriter(), comment.getIsReported(), comment.getCreatedAt());
    }

    public CommunityPostMemberVo toPostVO(CategoryPostMemberDao dao) {
        val member = dao.posts().getIsBlindWriter() ? null : toMemberResponse(dao.member());
        val category = toCategoryResponse(dao.category());
        return new CommunityPostMemberVo(member, dao.posts(),category);
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
        return new CategoryVo(category.getId(), category.getName());
    }

    public PostResponse toPostResponse (CommunityPostMemberVo dao, List<CommentDao> commentDaos, Long memberId) {
        val post = dao.posts();
        val category = dao.category();
        val member = dao.posts().getIsBlindWriter() ? null : dao.member();
        val isMine = Objects.equals(dao.member().id(), memberId);
        val comments = commentDaos.stream().map(comment -> toCommentResponse(comment, memberId)).collect(Collectors.toList());
        return new PostResponse(post.getId(), member, isMine, post.getCategoryId(), category.name(), post.getTitle(), post.getContent(), post.getHits(),
                comments.size(), post.getImages(), post.getIsQuestion(), post.getIsBlindWriter(), post.getCreatedAt(), comments);
    }
}
