package org.sopt.makers.internal.mapper;

import lombok.val;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.dto.community.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommunityResponseMapper {
    public CommentResponse toCommentResponse(CommentDao dao) {
        val member = toMemberResponse(dao.member());
        val comment = dao.comment();
        return new CommentResponse(comment.getId(), member, comment.getPostId(), comment.getParentCommentId(),
                comment.getContent(), comment.getIsBlindWriter(), comment.getIsReported(), comment.getCreatedAt());
    }

    public CommunityPostMemberVo toPostVO(CategoryPostMemberDao dao) {
        val member = toMemberResponse(dao.member());
        return new CommunityPostMemberVo(member, dao.posts());
    }

    public MemberVo toMemberResponse(Member member) {
        val career = member.getCareers().stream().filter(MemberCareer::getIsCurrent).toList();
        member.getActivities().sort((act1, act2) -> (act2.getGeneration() - act1.getGeneration()));
        return new MemberVo(member.getId(),member.getName(), member.getProfileImage(),
                member.getActivities().get(0), career.isEmpty() ? null : member.getCareers().get(0));
    }

    public PostResponse toPostResponse (CommunityPostMemberVo dao, List<CommentDao> commentDaos) {
        val post = dao.posts();
        val member = dao.member();
        val comments = commentDaos.stream().map(this::toCommentResponse).collect(Collectors.toList());
        return new PostResponse(post.getId(),member, post.getWriterId(), post.getTitle(), post.getContent(),
                post.getHits(), post.getImages(), post.getIsQuestion(), post.getIsBlindWriter(), post.getCreatedAt(), comments);
    }
}
