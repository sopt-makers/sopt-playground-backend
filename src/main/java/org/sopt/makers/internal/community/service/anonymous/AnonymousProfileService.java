package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AnonymousProfileService {

	private static final int RECENT_NICKNAME_LIMIT = 50;

	private final AnonymousProfileRetriever anonymousProfileRetriever;
	private final AnonymousProfileModifier anonymousProfileModifier;
	private final AnonymousProfileImageRetriever anonymousProfileImageRetriever;
	private final AnonymousNicknameRetriever anonymousNicknameRetriever;

	/**
	 * Member + Post 조합으로 익명 프로필 조회 또는 생성
	 * 같은 사용자가 같은 게시글에서는 항상 같은 익명 프로필 사용
	 */
	@Transactional
	public AnonymousProfile getOrCreateAnonymousProfile(Member member, CommunityPost post) {
		return anonymousProfileRetriever
				.findByMemberAndPost(member, post)
				.orElseGet(() -> createAnonymousProfile(member, post));
	}

	/**
	 * 새로운 익명 프로필 생성
	 */
	private AnonymousProfile createAnonymousProfile(Member member, CommunityPost post) {
		// 해당 게시글에서 이미 사용된 닉네임 제외
		List<AnonymousProfile> existingProfilesInPost = anonymousProfileRetriever.findAllByPostId(post.getId());
		List<AnonymousNickname> excludeNicknamesInPost = existingProfilesInPost.stream()
				.map(AnonymousProfile::getNickname)
				.toList();

		// 최근 50개 프로필에서 사용된 닉네임도 제외
		List<AnonymousProfile> recentProfiles = anonymousProfileRetriever.getTopByOrderByCreatedAt(RECENT_NICKNAME_LIMIT);
		List<AnonymousNickname> recentNicknames = recentProfiles.stream()
				.map(AnonymousProfile::getNickname)
				.toList();

		// 중복 없이 합치기
		List<AnonymousNickname> excludeNicknames = Stream.concat(
				excludeNicknamesInPost.stream(),
				recentNicknames.stream()
		).distinct().toList();

		AnonymousNickname nickname = anonymousNicknameRetriever.findRandomAnonymousNickname(excludeNicknames);
		AnonymousProfileImage profileImg = anonymousProfileImageRetriever.getAnonymousProfileImage();

		return anonymousProfileModifier.createAnonymousProfile(member, post, nickname, profileImg);
	}
}
