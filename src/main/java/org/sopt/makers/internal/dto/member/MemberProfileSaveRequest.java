package org.sopt.makers.internal.dto.member;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.YearMonth;
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
		List<MemberLinkSaveRequest> links,
		List<MemberSoptActivitySaveRequest> activities,
		List<MemberCareerSaveRequest> careers,
		Boolean openToWork,
		Boolean openToSideProject,
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
