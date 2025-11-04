package org.sopt.makers.internal.community.service.comment;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.comment.CommunityCommentLike;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentLikeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommunityCommentLikeRetriever {

	private final CommunityCommentLikeRepository commentLikeRepository;

	/**
	 * 특정 사용자가 특정 댓글에 좋아요를 눌렀는지 확인
	 */
	public boolean isLiked(Long memberId, Long commentId) {
		return commentLikeRepository.existsByMemberIdAndCommentId(memberId, commentId);
	}

	/**
	 * 특정 사용자의 특정 댓글 좋아요 엔티티 조회
	 */
	public Optional<CommunityCommentLike> findByMemberIdAndCommentId(Long memberId, Long commentId) {
		return commentLikeRepository.findByMemberIdAndCommentId(memberId, commentId);
	}

	/**
	 * 특정 댓글의 총 좋아요 수 조회
	 */
	public Integer countLikes(Long commentId) {
		return commentLikeRepository.countAllByCommentId(commentId);
	}
}
