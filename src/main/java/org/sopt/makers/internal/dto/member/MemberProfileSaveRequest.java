package org.sopt.makers.internal.dto.member;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileSaveRequest(
		@Schema(required = true)
		String name,
		String profileImage,
		LocalDate birthday,
		String phone,
		String email,
		String address,
		String university,
		String major,
		String introduction,
		String skill,
		String mbti,
		String mbtiDescription,
		Double sojuCapacity,
		String interest,
		UserFavorRequest userFavor,
		String idealType,
		String selfIntroduction,
		List<MemberLinkSaveRequest> links,
		@Schema(required = true)
		List<MemberSoptActivitySaveRequest> activities,
		List<MemberCareerSaveRequest> careers,
		Boolean allowOfficial
) {
	public record UserFavorRequest(
			Boolean isPourSauceLover,
			Boolean isHardPeachLover,
			Boolean isMintChocoLover,
			Boolean isRedBeanFishBreadLover,
			Boolean isSojuLover,
			Boolean isRiceTteokLover
	){}

	public record MemberLinkSaveRequest(
			String title,
			String url
	){}

	public record MemberSoptActivitySaveRequest(
			Integer generation,
			String part,
			String team
	){}

	public record MemberCareerSaveRequest(
			String companyName,
			String title,

			@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
			String startDate,

			@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
			String endDate,
			Boolean isCurrent
	){}
}
