package org.sopt.makers.internal.member.service.sorting.strategy;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;
import org.springframework.stereotype.Component;

/**
 * 기본 프로필 가중치 전략
 * - 프로필 이미지: 5점
 * - 자기소개: 3점
 * - 커리어 정보: 개당 3점
 * - 링크: 개당 1점
 * - 기타 정보: 각 1점
 *
 * <p>필드가 채워졌는지 판정하는 책임은 {@link MemberProfileSummaryVo} 하위 VO 들이 가진다.
 * 이 클래스는 각 항목에 몇 점을 줄지만 결정한다.
 */
@Component
public class DefaultProfileWeightStrategy implements ProfileWeightStrategy {

	private static final int PROFILE_IMAGE_WEIGHT = 5;
	private static final int INTRODUCTION_WEIGHT = 3;
	private static final int CAREER_WEIGHT = 3;
	private static final int LINK_WEIGHT = 1;
	private static final int OTHER_FIELD_WEIGHT = 1;

	@Override
	public int calculate(InternalUserDetails userDetails, MemberProfileSummaryVo member) {
		int weight = 0;

		// 플랫폼 서버에서 오는 값
		if (isFilled(userDetails.profileImage())) {
			weight += PROFILE_IMAGE_WEIGHT;
		}
		if (isFilled(userDetails.birthday())) {
			weight += OTHER_FIELD_WEIGHT;
		}
		if (isFilled(userDetails.phone())) {
			weight += OTHER_FIELD_WEIGHT;
		}
		if (isFilled(userDetails.email())) {
			weight += OTHER_FIELD_WEIGHT;
		}

		if (member == null) {
			return weight;
		}

		// 플레이그라운드 로컬 프로필
		if (member.intro().hasIntroduction()) {
			weight += INTRODUCTION_WEIGHT;
		}
		if (member.intro().hasSelfIntroduction()) {
			weight += OTHER_FIELD_WEIGHT;
		}
		if (member.intro().hasSkill()) {
			weight += OTHER_FIELD_WEIGHT;
		}

		weight += member.basicInfo().filledCount() * OTHER_FIELD_WEIGHT;
		weight += member.personality().filledCount() * OTHER_FIELD_WEIGHT;
		weight += member.favor().answeredCount() * OTHER_FIELD_WEIGHT;

		weight += member.activity().linkCount() * LINK_WEIGHT;
		weight += member.activity().careerCount() * CAREER_WEIGHT;

		return weight;
	}

	private boolean isFilled(String value) {
		return value != null && !value.isBlank();
	}
}
