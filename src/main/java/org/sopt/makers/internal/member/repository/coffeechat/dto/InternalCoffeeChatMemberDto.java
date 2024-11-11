package org.sopt.makers.internal.member.repository.coffeechat.dto;

import org.sopt.makers.internal.domain.Member;

import java.util.List;

public record InternalCoffeeChatMemberDto(
		Long id,
		List<String> parts,
		String name,
		String profileImage
) {
	public static InternalCoffeeChatMemberDto of(Member member, List<String> parts) {
		return new InternalCoffeeChatMemberDto(
				member.getId(), parts, member.getName(), member.getProfileImage()
		);
	}
}
