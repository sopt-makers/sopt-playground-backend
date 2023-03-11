package org.sopt.makers.internal.dto.member;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileSaveRequest(
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
		Boolean isPourSauceLover,
		Boolean isHardPeachLover,
		Boolean isMintChocoLover,
		Boolean isRedBeanLover,
		Boolean isSojuLover,
		Boolean isRiceTteokLover,
		String idealType,
		String selfIntroduction,
		List<MemberLinkSaveRequest> links,
		List<MemberSoptActivitySaveRequest> activities,
		List<MemberCareerSaveRequest> careers,
		Boolean allowOfficial
) {

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
