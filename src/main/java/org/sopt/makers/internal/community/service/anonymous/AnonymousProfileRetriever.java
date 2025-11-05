package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousProfileRepository;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AnonymousProfileRetriever {

	private final AnonymousProfileRepository anonymousProfileRepository;

	public Optional<AnonymousProfile> findByMemberAndPost(Member member, CommunityPost post) {
		return anonymousProfileRepository.findByMemberAndPost(member, post);
	}

	public Optional<AnonymousProfile> findByPostId(Long postId) {
		return anonymousProfileRepository.findByPostId(postId);
	}

	public List<AnonymousProfile> findAllByPostId(Long postId) {
		return anonymousProfileRepository.findAllByPostId(postId);
	}

	public List<AnonymousProfile> getTopByOrderByCreatedAt(int limit) {
		return anonymousProfileRepository.findTopByOrderByIdDescWithLimit(limit);
	}

	public List<String> findNicknamesByPostIdAndNicknamesIn(Long postId, List<String> nicknames) {
		return anonymousProfileRepository.findNicknamesByPostIdAndNicknamesIn(postId, nicknames);
	}

	public List<AnonymousProfile> findByPostIdAndNicknames(Long postId, List<String> nicknames) {
		if (nicknames == null || nicknames.isEmpty()) {
			return Collections.emptyList();
		}
		return anonymousProfileRepository.findByPostIdAndNicknamesIn(postId, nicknames);
	}

	public Long[] extractUserIds(List<AnonymousProfile> profiles) {
		if (profiles == null || profiles.isEmpty()) {
			return new Long[0];
		}

		return profiles.stream()
				.map(profile -> profile.getMember().getId())
				.distinct()
				.toArray(Long[]::new);
	}
}
