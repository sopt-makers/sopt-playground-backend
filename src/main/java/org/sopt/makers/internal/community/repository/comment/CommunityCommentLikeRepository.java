package org.sopt.makers.internal.community.repository.comment;

import org.sopt.makers.internal.community.domain.comment.CommunityCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommunityCommentLikeRepository extends JpaRepository<CommunityCommentLike, Long> {

	// READ
	Boolean existsByMemberIdAndCommentId(Long memberId, Long commentId);

	Optional<CommunityCommentLike> findByMemberIdAndCommentId(Long memberId, Long commentId);

	Integer countAllByCommentId(Long commentId);

	Integer countAllByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

	List<CommunityCommentLike> findAllByMemberIdAndCommentIdIn(Long memberId, List<Long> commentIds);

	@Query(value = "SELECT c.comment.id, COUNT(c) FROM CommunityCommentLike c WHERE c.comment.id IN :commentIds GROUP BY c.comment.id")
	List<Object[]> countLikesByCommentIds(@Param("commentIds") List<Long> commentIds);
}
