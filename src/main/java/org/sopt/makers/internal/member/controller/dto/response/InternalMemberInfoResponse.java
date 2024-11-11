package org.sopt.makers.internal.member.controller.dto.response;

public record InternalMemberInfoResponse(
		Long id,
		String profileImage,
		Integer soptProjectCount
) {
}
