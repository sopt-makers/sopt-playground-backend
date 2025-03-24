package org.sopt.makers.internal.resolution.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.exception.ClientBadRequestException;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ResolutionTag {

	PRODUCT_RELEASE("제품 출시"),
	NETWORKING("네트워킹"),
	COLLABORATION_EXPERIENCE("협업 경험"),
	STARTUP("창업"),
	SKILL_UP("스킬업");

	private final String description;

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