package org.sopt.makers.internal.resolution.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.resolution.domain.ResolutionTag;
import org.sopt.makers.internal.resolution.domain.UserResolution;

import javax.validation.constraints.NotBlank;

public record ResolutionSaveRequest(
	List<String> tags,
	@Schema(required = true)
	@NotBlank(message = "Content cannot be empty or blank.")
	String content
) {
	public UserResolution toDomain(Member member, Integer generation) {
		return UserResolution.builder()
				.member(member)
				.resolutionTags(ResolutionTag.fromStringArray(tags))
				.content(content)
				.generation(generation)
				.build();
	}
}
