package org.sopt.makers.internal.member.domain.coffeechat;

import lombok.Getter;

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
}
