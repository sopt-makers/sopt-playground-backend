package org.sopt.makers.internal.community.repository.comment;

import org.sopt.makers.internal.community.domain.comment.CommunityCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {

	// READ
	Boolean existsByMemberIdAndCommentId(Long memberId, Long commentId);

	Optional<CommunityCommentLike> findByMemberIdAndCommentId(Long memberId, Long commentId);

	Integer countAllByCommentId(Long commentId);

	Integer countAllByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);
}
