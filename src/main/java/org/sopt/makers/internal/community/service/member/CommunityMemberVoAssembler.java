package org.sopt.makers.internal.community.service.member;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.dto.MemberVo;
import org.sopt.makers.internal.external.platform.InternalUserDetails;
import org.sopt.makers.internal.external.platform.PlatformService;
import org.sopt.makers.internal.member.domain.MemberCareer;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityMemberVoAssembler {

	private final PlatformService platformService;
	private final MemberCareerRetriever memberCareerRetriever;

	public Map<Long, MemberVo> getMemberVoMap(List<Long> memberIds) {
		if (memberIds == null || memberIds.isEmpty()) {
			return Map.of();
		}

		List<Long> distinctMemberIds = memberIds.stream()
			.filter(Objects::nonNull)
			.distinct()
			.toList();

		if (distinctMemberIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, InternalUserDetails> userDetailsMap =
			platformService.getInternalUserDetailsMap(distinctMemberIds);

		Map<Long, MemberCareer> careerMap =
			memberCareerRetriever.findMemberLastCareerMapByMemberIds(distinctMemberIds);

		Map<Long, MemberVo> memberVoMap = new LinkedHashMap<>();

		for (Long memberId : distinctMemberIds) {
			InternalUserDetails userDetails = userDetailsMap.get(memberId);

			if (userDetails == null) {
				continue;
			}

			memberVoMap.put(
				memberId,
				MemberVo.of(userDetails, careerMap.get(memberId))
			);
		}

		return memberVoMap;
	}

	public MemberVo getMemberVo(Long memberId) {
		return getMemberVoMap(List.of(memberId)).get(memberId);
	}
}