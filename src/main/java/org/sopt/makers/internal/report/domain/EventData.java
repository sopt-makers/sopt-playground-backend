package org.sopt.makers.internal.report.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventData {
	TOTAL_VISIT_COUNT("session_start", ""),
	MEMBER_TAB_VISIT_COUNT("[Amplitude] Page Viewed", "/members"),
	PROJECT_TAB_VISIT_COUNT("[Amplitude] Page Viewed", "/projects"),
	COFFEE_CHAT_TAB_VISIT_COUNT("[Amplitude] Page Viewed", "/coffeechat"),
	CREW_TAB_VISIT_COUNT("[Amplitude] Page Viewed", "/group/"),
	MEMBER_PROFILE_CARD_VIEW_COUNT("Click-memberCard", ""),
	;

	private final String property;
	private final String pagePath;
}
