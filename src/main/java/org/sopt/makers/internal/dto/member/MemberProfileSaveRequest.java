package org.sopt.makers.internal.dto.member;

import java.time.LocalDate;
import java.util.List;

public record MemberProfileSaveRequest(
        String profileImage,
		String name,
		LocalDate birthday,
		String phone,
		String email,
		String address,
		String university,
		String major,
		String introduction,
		String skill,
		List<MemberLinkSaveRequest> links,
		List<MemberProjectSaveRequest> projects,
		Boolean openToWork,
		Boolean openToSideProject,
		Boolean allowOfficial
) {

	public record MemberLinkSaveRequest(
			Long linkId,
			String linkTitle,
			String linkUrl
	){}

	public record MemberProjectSaveRequest(
			Integer generation,
			String type,
			String teamName
	){}
}
