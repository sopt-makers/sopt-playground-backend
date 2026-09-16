package org.sopt.makers.internal.member.service.sorting.strategy;

import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.member.dto.profile.MemberProfileSummaryVo;

/**
 * 프로필 정보 가중치 전략 인터페이스
 */
public interface ProfileWeightStrategy {
	int calculate(InternalUserDetails userDetails, MemberProfileSummaryVo member);
}
