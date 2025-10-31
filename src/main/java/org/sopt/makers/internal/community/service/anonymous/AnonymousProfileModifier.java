package org.sopt.makers.internal.community.service.anonymous;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfile;
import org.sopt.makers.internal.community.domain.anonymous.AnonymousProfileImage;
import org.sopt.makers.internal.community.repository.anonymous.AnonymousProfileRepository;
import org.sopt.makers.internal.member.domain.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnonymousProfileModifier {

	private final AnonymousProfileRepository anonymousProfileRepository;

	/**
	 * 익명 프로필 생성 및 저장
	 */
	public AnonymousProfile createAnonymousProfile(
			Member member,
			CommunityPost post,
			AnonymousNickname nickname,
			AnonymousProfileImage profileImg
	) {
		return anonymousProfileRepository.save(
				AnonymousProfile.builder()
						.member(member)
						.post(post)
						.nickname(nickname)
						.profileImg(profileImg)
						.build()
		);
	}
}
