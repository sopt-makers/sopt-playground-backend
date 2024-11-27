package org.sopt.makers.internal.repository.community;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.community.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousPostProfileRepository extends JpaRepository<AnonymousPostProfile, Long> {

	// CREATE

	// READ
	Optional<AnonymousPostProfile> findAnonymousPostProfileByMemberIdAndCommunityPostId(Long memberId, Long communityPostId);

	Optional<AnonymousPostProfile> findByMemberAndCommunityPost(Member member, CommunityPost post);

	List<AnonymousPostProfile> findTop4ByOrderByCreatedAtDesc();

	List<AnonymousPostProfile> findTop50ByOrderByCreatedAtDesc();

	// UPDATE

	//DELETE
}
