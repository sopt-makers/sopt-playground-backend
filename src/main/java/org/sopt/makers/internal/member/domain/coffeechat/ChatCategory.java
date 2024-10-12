package org.sopt.makers.internal.member.domain.coffeechat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.sopt.makers.internal.exception.ClientBadRequestException;

import java.util.Arrays;

@Getter
public enum ChatCategory {
	COFFEE_CHAT("커피챗"),
	FRIENDSHIP("친목"),
	APPJAM_TEAM_BUILD("앱잼 팀 빌딩"),
	PROJECT_PROPOSAL("프로젝트 제안"),
	OTHER("기타")
	;

	final String value;

	ChatCategory(String value) {
		this.value = value;
	}

	@JsonCreator
	public static ChatCategory fromValue(String value) {
		return Arrays.stream(ChatCategory.values())
				.filter(category -> category.value.equals(value))
				.findFirst()
				.orElseThrow(() -> new ClientBadRequestException("Unknown Chat Category Value: " + value));
	}

	@JsonValue
	public String getValue() {
		return value;
	}
}
