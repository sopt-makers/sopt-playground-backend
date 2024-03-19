package org.sopt.makers.internal.resolution.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

	private static final Map<String, ResolutionTag> TAG_MAP = Arrays.stream(ResolutionTag.values())
		.collect(Collectors.toMap(tag -> tag.value, tag -> tag));

	ResolutionTag(String value, int index) {
		this.value = value;
		this.index = index;
	}

	public static String getTagIds(List<String> tagNames) {
		return tagNames.stream()
			.map(tag -> String.valueOf(TAG_MAP.get(validate(tag)).index))
			.collect(Collectors.joining(","));
	}

	public static String validate(String value) {
		if (TAG_MAP.get(value) == null) {
			throw new ClientBadRequestException("Unknown Tag Name");
		}
		return value;
	}

	public static List<String> getTagNames(String tagIds) {
		List<String> indexList = Arrays.asList(tagIds.split(","));

		return Arrays.stream(ResolutionTag.values())
				.filter(tag -> indexList.contains(String.valueOf(tag.index)))
				.map(tag -> tag.value)
				.collect(Collectors.toList());
	}
}