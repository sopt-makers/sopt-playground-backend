package org.sopt.makers.internal.dto.member;

import org.sopt.makers.internal.domain.MemberSoptActivity;

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
		List<MemberLinkSaveRequest> links,
		List<MemberSoptActivitySaveRequest> activities,
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
			String category,
			String teamName
	){}
}
