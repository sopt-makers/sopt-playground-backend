package org.sopt.makers.internal.coffeechat.dto.request;

import org.sopt.makers.internal.member.domain.Member;

import java.util.List;

public record InternalCoffeeChatMemberDto(
		Long id,
		String name,
		List<String> parts,
		String profileImage
) {
	public static InternalCoffeeChatMemberDto of(Member member, List<String> parts) {
		return new InternalCoffeeChatMemberDto(
				member.getId(), member.getName(), parts, member.getProfileImage()
		);
	}
}
