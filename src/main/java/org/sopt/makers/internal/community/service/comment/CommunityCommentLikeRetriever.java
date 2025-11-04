package org.sopt.makers.internal.community.service.comment;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.comment.CommunityCommentLike;
import org.sopt.makers.internal.community.repository.comment.CommunityCommentLikeRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommunityCommentLikeRetriever {

	private final CommunityCommentLikeRepository commentLikeRepository;

	public boolean isLiked(Long memberId, Long commentId) {
		return commentLikeRepository.existsByMemberIdAndCommentId(memberId, commentId);
	}

	public Optional<CommunityCommentLike> findByMemberIdAndCommentId(Long memberId, Long commentId) {
		return commentLikeRepository.findByMemberIdAndCommentId(memberId, commentId);
	}

	public Integer countLikes(Long commentId) {
		return commentLikeRepository.countAllByCommentId(commentId);
	}

	public Map<Long, Boolean> getLikedMapByCommentIds(Long memberId, List<Long> commentIds) {
		if (commentIds == null || commentIds.isEmpty()) {
			return new HashMap<>();
		}

		List<CommunityCommentLike> likedComments = commentLikeRepository.findAllByMemberIdAndCommentIdIn(memberId, commentIds);
		Map<Long, Boolean> likedMap = likedComments.stream()
				.collect(Collectors.toMap(
						like -> like.getComment().getId(),
						like -> true
				));
		for (Long commentId : commentIds) {
			likedMap.putIfAbsent(commentId, false);
		}

		return likedMap;
	}

	public Map<Long, Integer> getLikeCountMapByCommentIds(List<Long> commentIds) {
		if (commentIds == null || commentIds.isEmpty()) {
			return new HashMap<>();
		}

		List<Object[]> results = commentLikeRepository.countLikesByCommentIds(commentIds);
		Map<Long, Integer> countMap = results.stream()
				.collect(Collectors.toMap(
						row -> (Long) row[0],
						row -> ((Long) row[1]).intValue()
				));
		for (Long commentId : commentIds) {
			countMap.putIfAbsent(commentId, 0);
		}

		return countMap;
	}
}
