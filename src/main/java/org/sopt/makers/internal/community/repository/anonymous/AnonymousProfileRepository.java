package org.sopt.makers.internal.community.repository.anonymous;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnonymousProfileRepository extends JpaRepository<AnonymousProfile, Long>, AnonymousProfileRepositoryCustom {

	// ===== 핵심 조회 메서드 =====

	// Member + Post 조합으로 프로필 조회 (핵심!)
	Optional<AnonymousProfile> findByMemberAndPost(Member member, CommunityPost post);

	// ===== Post 기준 조회 =====

	// Post ID로 단일 프로필 조회 (게시글 작성자용)
	Optional<AnonymousProfile> findByPostId(Long postId);

	// Post ID로 모든 익명 프로필 조회 (게시글의 모든 참여자)
	List<AnonymousProfile> findAllByPostId(Long postId);

	// ===== 익명 멘션 검증용 쿼리 =====

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
}
