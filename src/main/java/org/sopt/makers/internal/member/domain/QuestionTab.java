package org.sopt.makers.internal.member.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionTab {
	ANSWERED("answered", "답변 완료"),
	UNANSWERED("unanswered", "새질문");

	private final String value;
	private final String description;

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static QuestionTab from(String value) {
		if (value == null) {
			return ANSWERED;
		}
		for (QuestionTab tab : QuestionTab.values()) {
			if (tab.value.equalsIgnoreCase(value)) {
				return tab;
			}
		}
		return ANSWERED;
	}
}
