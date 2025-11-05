package org.sopt.makers.internal.community.service.comment;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.domain.comment.CommunityCommentLike;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentLikeRepository;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityCommentLikeModifier {

	private final CommunityCommentLikeRepository commentLikeRepository;

	public CommunityCommentLike create(Member member, CommunityComment comment) {
		CommunityCommentLike commentLike = CommunityCommentLike.builder()
				.member(member)
				.comment(comment)
				.build();
		return commentLikeRepository.save(commentLike);
	}

	public void delete(CommunityCommentLike commentLike) {
		commentLikeRepository.delete(commentLike);
	}
}
