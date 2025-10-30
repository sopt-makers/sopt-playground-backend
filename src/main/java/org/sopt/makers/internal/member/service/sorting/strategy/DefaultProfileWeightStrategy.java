package org.sopt.makers.internal.member.service.sorting.strategy;

import java.util.Objects;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.member.domain.UserFavor;
import org.springframework.stereotype.Component;

/**
 * 기본 프로필 가중치 전략
 * - 프로필 이미지: 5점
 * - 자기소개: 3점
 * - 커리어 정보: 개당 3점
 * - 링크: 개당 1점
 * - 기타 정보: 각 1점
 */
@Component
public class DefaultProfileWeightStrategy implements ProfileWeightStrategy {

	private static final int PROFILE_IMAGE_WEIGHT = 5;
	private static final int INTRODUCTION_WEIGHT = 3;
	private static final int CAREER_WEIGHT = 3;
	private static final int LINK_WEIGHT = 1;
	private static final int OTHER_FIELD_WEIGHT = 1;

	@Override
	public int calculate(InternalUserDetails userDetails, Member member) {
		int weight = 0;

		if (userDetails.profileImage() != null && !userDetails.profileImage().isBlank()) {
			weight += PROFILE_IMAGE_WEIGHT;
		}

		if (userDetails.birthday() != null && !userDetails.birthday().isBlank()) {
			weight += OTHER_FIELD_WEIGHT;
		}
		if (userDetails.phone() != null && !userDetails.phone().isBlank()) {
			weight += OTHER_FIELD_WEIGHT;
		}
		if (userDetails.email() != null && !userDetails.email().isBlank()) {
			weight += OTHER_FIELD_WEIGHT;
		}

		if (member != null) {
			if (member.getIntroduction() != null && !member.getIntroduction().isBlank()) {
				weight += INTRODUCTION_WEIGHT;
			}

			if (member.getAddress() != null && !member.getAddress().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getUniversity() != null && !member.getUniversity().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getMajor() != null && !member.getMajor().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getSkill() != null && !member.getSkill().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getMbti() != null && !member.getMbti().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getMbtiDescription() != null && !member.getMbtiDescription().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getSojuCapacity() != null) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getInterest() != null && !member.getInterest().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getIdealType() != null && !member.getIdealType().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}
			if (member.getSelfIntroduction() != null && !member.getSelfIntroduction().isBlank()) {
				weight += OTHER_FIELD_WEIGHT;
			}

			UserFavor favor = member.getUserFavor();
			if (favor != null) {
				if (favor.getIsPourSauceLover() != null) {
					weight += OTHER_FIELD_WEIGHT;
				}
				if (favor.getIsHardPeachLover() != null) {
					weight += OTHER_FIELD_WEIGHT;
				}
				if (favor.getIsMintChocoLover() != null) {
					weight += OTHER_FIELD_WEIGHT;
				}
				if (favor.getIsRedBeanFishBreadLover() != null) {
					weight += OTHER_FIELD_WEIGHT;
				}
				if (favor.getIsSojuLover() != null) {
					weight += OTHER_FIELD_WEIGHT;
				}
				if (favor.getIsRiceTteokLover() != null) {
					weight += OTHER_FIELD_WEIGHT;
				}
			}

			if (member.getLinks() != null && !member.getLinks().isEmpty()) {
				weight += member.getLinks().size() * LINK_WEIGHT;
			}

			if (member.getCareers() != null && !member.getCareers().isEmpty()) {
				weight += member.getCareers().size() * CAREER_WEIGHT;
			}
		}

		return weight;
	}
}
