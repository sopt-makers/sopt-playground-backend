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

	final String title;

	ChatCategory(String title) {
		this.title = title;
	}

	@JsonCreator
	public static ChatCategory fromTitle(String title) {
		return Arrays.stream(ChatCategory.values())
				.filter(category -> category.title.equals(title))
				.findFirst()
				.orElseThrow(() -> new ClientBadRequestException("Unknown Chat Category Title: " + title));
	}

	@JsonValue
	public String getTitle() {
		return title;
	}
}
