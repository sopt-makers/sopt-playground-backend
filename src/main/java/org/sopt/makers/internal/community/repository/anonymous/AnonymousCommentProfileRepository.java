package org.sopt.makers.internal.community.repository.anonymous;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.community.domain.anonymous.AnonymousCommentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousCommentProfileRepository extends JpaRepository<AnonymousCommentProfile, Long> {

	List<AnonymousCommentProfile> findAllByCommunityCommentPostId(Long postId);

	Optional<AnonymousCommentProfile> findByMemberIdAndCommunityCommentPostId(Long memberId, Long postId);
}
