package org.sopt.makers.internal.community.repository.anonymous;

import java.util.Optional;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousPostProfile;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousPostProfileRepository extends JpaRepository<AnonymousPostProfile, Long>, AnonymousPostProfileRepositoryCustom {

	// CREATE

	// READ
	Optional<AnonymousPostProfile> findAnonymousPostProfileByMemberIdAndCommunityPostId(Long memberId, Long communityPostId);
	Optional<AnonymousPostProfile> findByMemberAndCommunityPost(Member member, CommunityPost post);

	// UPDATE

	//DELETE
}
