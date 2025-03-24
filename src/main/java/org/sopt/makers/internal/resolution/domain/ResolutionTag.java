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

	@JsonCreator
	public static ResolutionTag fromString(String value) {
		try {
			return ResolutionTag.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new ClientBadRequestException("Unknown Timecapsop Tag Name: " + value);
		}
	}

	public static List<ResolutionTag> fromStringArray(List<String> values) {
		return values.stream()
				.map(ResolutionTag::fromString)
				.toList();
	}
}