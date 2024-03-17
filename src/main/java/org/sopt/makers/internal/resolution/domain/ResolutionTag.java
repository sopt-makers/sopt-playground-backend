package org.sopt.makers.internal.resolution.domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.sopt.makers.internal.exception.ClientBadRequestException;

public enum ResolutionTag {

	SKILL_ENHANCEMENT("능력 향상", 1),
	IT_KNOWLEDGE("IT 지식", 2),
	FRIENDSHIP("친목", 3),
	ENTREPRENEURSHIP_FOUNDATION("창업 기반", 4),
	COLLABORATION_EXPERIENCE("협업 경험", 5),
	GREAT_TEAM("좋은 팀", 6);

	private final String value;
	private final int index;

	ResolutionTag(String value, int index) {
		this.value = value;
		this.index = index;
	}

	public static String getTagIds(List<String> tagNames) {
		return tagNames.stream()
			.map(tag -> String.valueOf(ResolutionTag.of(tag).index))
			.collect(Collectors.joining(","));
	}

	public static ResolutionTag of(String value) {
		return Arrays.stream(ResolutionTag.values())
			.filter(tag -> value.equals(tag.value))
			.findFirst()
			.orElseThrow(() -> new ClientBadRequestException("Unknown Tag Name"));
	}
}
