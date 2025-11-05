package org.sopt.makers.internal.community.repository.anonymous;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnonymousProfileRepository extends JpaRepository<AnonymousProfile, Long>, AnonymousProfileRepositoryCustom {

	@EntityGraph(attributePaths = {"nickname", "profileImg"})
	Optional<AnonymousProfile> findByMemberAndPost(Member member, CommunityPost post);

	@Query("""
		SELECT ap FROM AnonymousProfile ap
		JOIN FETCH ap.nickname
		JOIN FETCH ap.profileImg
		WHERE ap.id = (SELECT cp.anonymousProfile.id FROM CommunityPost cp WHERE cp.id = :postId)
		""")
	Optional<AnonymousProfile> findByPostId(@Param("postId") Long postId);

	@EntityGraph(attributePaths = {"nickname", "profileImg"})
	List<AnonymousProfile> findAllByPostId(Long postId);

	@Query("""
		SELECT DISTINCT ap.nickname.nickname
		FROM AnonymousProfile ap
		WHERE ap.post.id = :postId
		AND ap.nickname.nickname IN :nicknames
		""")
	List<String> findNicknamesByPostIdAndNicknamesIn(
		@Param("postId") Long postId,
		@Param("nicknames") List<String> nicknames
	);

	@Query("""
      SELECT DISTINCT ap
      FROM AnonymousProfile ap
      JOIN FETCH ap.member
      JOIN FETCH ap.nickname
      WHERE ap.post.id = :postId
      AND ap.nickname.nickname IN :nicknames
      """)
	List<AnonymousProfile> findByPostIdAndNicknamesIn(
			@Param("postId") Long postId,
			@Param("nicknames") List<String> nicknames
	);
}
