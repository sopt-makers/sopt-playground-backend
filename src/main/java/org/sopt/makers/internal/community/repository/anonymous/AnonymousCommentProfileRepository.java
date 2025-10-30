package org.sopt.makers.internal.community.repository.anonymous;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.community.domain.anonymous.AnonymousCommentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnonymousCommentProfileRepository extends JpaRepository<AnonymousCommentProfile, Long> {

	List<AnonymousCommentProfile> findAllByCommunityCommentPostId(Long postId);

	Optional<AnonymousCommentProfile> findByMemberIdAndCommunityCommentPostId(Long memberId, Long postId);

	Optional<AnonymousCommentProfile> findByCommunityCommentId(Long commentId);

	@Query("""
			SELECT DISTINCT acp.nickname.nickname
			FROM AnonymousCommentProfile acp
			WHERE acp.communityComment.postId = :postId
			AND acp.nickname.nickname IN :nicknames
			""")
	List<String> findNicknamesByPostIdAndNicknamesIn(
			@Param("postId") Long postId,
			@Param("nicknames") List<String> nicknames
	);
}
