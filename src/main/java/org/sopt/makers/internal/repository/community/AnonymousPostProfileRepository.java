package org.sopt.makers.internal.repository.community;

import java.util.Optional;

import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.community.AnonymousPostProfile;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousPostProfileRepository extends JpaRepository<AnonymousPostProfile, Long> {
	Optional<AnonymousPostProfile> findByMemberAndCommunityPost(Member member, CommunityPost post);
}
