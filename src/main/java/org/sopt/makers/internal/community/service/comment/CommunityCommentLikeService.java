package org.sopt.makers.internal.community.service.comment;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.comment.CommunityComment;
import org.sopt.makers.internal.community.domain.comment.CommunityCommentLike;
import org.sopt.makers.internal.community.service.post.CommunityPostRetriever;
import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommunityCommentLikeService {

	private final CommunityPostRetriever communityPostRetriever;
	private final CommunityCommentLikeModifier commentLikeModifier;
	private final CommunityCommentLikeRetriever commentLikeRetriever;
	private final CommunityCommentsRetriever commentsRetriever;
	private final MemberRetriever memberRetriever;

	@Transactional
	public void addCommentLike(Long memberId, Long postId, Long commentId) {
		CommunityPost post = communityPostRetriever.findCommunityPostById(postId);
		CommunityComment comment = commentsRetriever.findCommunityCommentById(commentId);
		if (!post.getComments().contains(comment)) {
			throw new ClientBadRequestException("해당 게시글의 댓글이 아닙니다.");
		}
		Member member = memberRetriever.findMemberById(memberId);

		boolean alreadyLiked = commentLikeRetriever.isLiked(memberId, commentId);
		if (alreadyLiked) {
			throw new ClientBadRequestException("이미 좋아요를 누른 댓글입니다.");
		}

		commentLikeModifier.create(member, comment);
	}

	@Transactional
	public void cancelCommentLike(Long memberId, Long postId, Long commentId) {
		CommunityPost post = communityPostRetriever.findCommunityPostById(postId);
		CommunityComment comment = commentsRetriever.findCommunityCommentById(commentId);
		if (!post.getComments().contains(comment)) {
			throw new ClientBadRequestException("해당 게시글의 댓글이 아닙니다.");
		}

		Optional<CommunityCommentLike> existingLike = commentLikeRetriever.findByMemberIdAndCommentId(memberId, commentId);
		if (existingLike.isEmpty()) {
			throw new ClientBadRequestException("좋아요를 누르지 않은 댓글입니다.");
		}

		commentLikeModifier.delete(existingLike.get());
	}

	@Transactional(readOnly = true)
	public boolean isLiked(Long memberId, Long commentId) {
		return commentLikeRetriever.isLiked(memberId, commentId);
	}

	@Transactional(readOnly = true)
	public Integer getLikeCount(Long commentId) {
		return commentLikeRetriever.countLikes(commentId);
	}

	@Transactional(readOnly = true)
	public Map<Long, Boolean> getLikedMapByCommentIds(Long memberId, List<Long> commentIds) {
		return commentLikeRetriever.getLikedMapByCommentIds(memberId, commentIds);
	}

	@Transactional(readOnly = true)
	public Map<Long, Integer> getLikeCountMapByCommentIds(List<Long> commentIds) {
		return commentLikeRetriever.getLikeCountMapByCommentIds(commentIds);
	}
}
