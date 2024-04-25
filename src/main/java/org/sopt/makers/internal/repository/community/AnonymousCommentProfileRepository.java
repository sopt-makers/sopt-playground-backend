package org.sopt.makers.internal.repository.community;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.domain.community.AnonymousCommentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousCommentProfileRepository extends JpaRepository<AnonymousCommentProfile, Long> {

	List<AnonymousCommentProfile> findAllByCommunityCommentPostId(Long postId);

	Optional<AnonymousCommentProfile> findByMemberIdAndCommunityCommentPostId(Long memberId, Long postId);
}
